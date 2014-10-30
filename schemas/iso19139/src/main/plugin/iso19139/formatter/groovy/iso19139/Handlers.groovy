package iso19139

import org.fao.geonet.services.metadata.format.groovy.Environment

public class Handlers {
    protected org.fao.geonet.services.metadata.format.groovy.Handlers handlers;
    protected org.fao.geonet.services.metadata.format.groovy.Functions f
    protected Environment env
    Matchers matchers
    Functions isofunc
    common.Handlers commonHandlers
    String[] packageViews

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        isofunc = new Functions(handlers: handlers, f:f, env:env)
        matchers =  new Matchers(handlers: handlers, f:f, env:env)
        commonHandlers = new common.Handlers(handlers, f, env)
        packageViews = [
                'gmd:identificationInfo', 'gmd:metadataMaintenance', 'gmd:metadataConstraints', 'gmd:spatialRepresentationInfo',
                'gmd:distributionInfo', 'gmd:applicationSchemaInfo', 'gmd:dataQualityInfo', 'gmd:portrayalCatalogueInfo',
                'gmd:contentInfo', 'gmd:metadataExtensionInfo']
    }

    def addDefaultHandlers() {
        handlers.add name: 'Text Elements', select: matchers.isTextEl, isoTextEl
        handlers.add name: 'URL Elements', select: matchers.isUrlEl, isoUrlEl
        handlers.add name: 'Simple Elements', select: matchers.isSimpleEl, isoSimpleEl
        handlers.add name: 'CodeList Elements', select: matchers.isCodeListEl, isoCodeListEl
        handlers.add name: 'Date Elements', select: matchers.isDateEl, dateEl
        handlers.add name: 'Elements with single Date child', select: matchers.hasDateChild, commonHandlers.applyToChild(isoCodeListEl, '*')
        handlers.add name: 'Elements with single Codelist child', select: matchers.hasCodeListChild, commonHandlers.applyToChild(isoCodeListEl, '*')
        handlers.add name: 'ResponsibleParty Elements', select: matchers.isRespParty, respPartyEl
        handlers.add 'gmd:contactInfo', commonHandlers.flattenedEntryEl({it.'gmd:CI_Contact'}, f.&nodeLabel)
        handlers.add 'gmd:address', commonHandlers.flattenedEntryEl({it.'gmd:CI_Address'}, f.&nodeLabel)
        handlers.add 'gmd:phone', commonHandlers.flattenedEntryEl({it.'gmd:CI_Telephone'}, f.&nodeLabel)
        handlers.add 'gmd:onlineResource', commonHandlers.flattenedEntryEl({it.'gmd:CI_OnlineResource'}, f.&nodeLabel)
        handlers.add 'gmd:CI_OnlineResource', commonHandlers.entryEl(f.&nodeLabel)
        handlers.add 'gmd:locale', localeEl
        handlers.add name: 'BBox Element', select: matchers.isBBox, bboxEl

        handlers.add name: 'Container Elements', select: matchers.isContainerEl, priority: -1, commonHandlers.entryEl(f.&nodeLabel)
        commonHandlers.addDefaultStartAndEndHandlers()

        handlers.sort name: 'Text Elements', select: 'gmd:MD_Metadata'/*matchers.isContainerEl*/, priority: -1, {el1, el2 ->
            def v1 = matchers.isContainerEl(el1) ? 1 : -1;
            def v2 = matchers.isContainerEl(el2) ? 1 : -1;
            return v1 - v2
        }
    }

    def isoTextEl = { commonHandlers.func.textEl(f.nodeLabel(it), isofunc.isoText(it))}
    def isoUrlEl = { commonHandlers.func.textEl(f.nodeLabel(it), it.'gmd:Url'.text())}
    def isoCodeListEl = {commonHandlers.func.textEl(f.nodeLabel(it), f.codelistValueLabel(it))}
    def isoSimpleEl = {commonHandlers.func.textEl(f.nodeLabel(it), it.'*'.text())}
    def dateEl = { commonHandlers.func.textEl(f.nodeLabel(it), it.text()); }

    def localeEl = { el ->
        def ptLocale = el.'gmd:PT_Locale'
        def toHtml = commonHandlers.when(matchers.isCodeListEl, commonHandlers.span(f.&codelistValueLabel))

        def data = [toHtml(ptLocale.'gmd:languageCode'.'gmd:LanguageCode'),
                    toHtml(ptLocale.'gmd:country'.'gmd:Country')]

        def nonEmptyEls = data.findAll{it != null}
        '<p> -- TODO Need widget for gmd:PT_Locale -- ' + nonEmptyEls.join("") + ' -- </p>'
    }

    /**
     * El must be a parent of gmd:CI_ResponsibleParty
     */
    def respPartyEl = {el ->
        def party = el.'gmd:CI_ResponsibleParty'

        def childrenToProcess = [
                party.'gmd:individualName',
                party.'gmd:organisationName',
                party.'gmd:positionName',
                party.'gmd:role',
                party.'gmd:contactInfo'.'*'.'*']
        def contactData = handlers.processElements( childrenToProcess )

        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData])
    }

    def bboxEl = {
        el ->
            def replacements = [
                    w: el.'gmd:westBoundLongitude'.'gco:Decimal'.text(),
                    e: el.'gmd:eastBoundLongitude'.'gco:Decimal'.text(),
                    s: el.'gmd:southBoundLatitude'.'gco:Decimal'.text(),
                    n: el.'gmd:northBoundLatitude'.'gco:Decimal'.text()
            ]

            def bboxData = handlers.fileResult("html/bbox.html", replacements)
            return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: bboxData])
    }

}
