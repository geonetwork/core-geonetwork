package iso19139

public class Handlers {
    def handlers;
    def f
    def env
    Matchers matchers
    Functions isofunc
    common.Handlers commonHandlers

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        isofunc = new Functions(handlers: handlers, f:f, env:env)
        matchers =  new Matchers(handlers: handlers, f:f, env:env)
        commonHandlers = new common.Handlers(handlers, f, env)
    }

    def addDefaultHandlers() {
        handlers.add name: 'Text Elements', select: matchers.isTextEl, isoTextEl
        handlers.add name: 'URL Elements', select: matchers.isUrlEl, isoUrlEl
        handlers.add name: 'CodeList Elements', select: matchers.isCodeListEl, isoCodeListEl
        handlers.add name: 'Elements with single Codelist child', select: matchers.hasCodeListChild, commonHandlers.applyToChild(isoCodeListEl, '*')
        handlers.add name: 'ResponsibleParty Elements', select: matchers.isRespParty, respPartyEl
        handlers.add 'gmd:contactInfo', contactInfoEl
        handlers.add 'gmd:address', addressEl

        handlers.add name: 'Container Elements', select: matchers.isContainerEl, processChildren: true, priority: -1, isoEntryEl
        commonHandlers.addDefaultStartAndEndHandlers()

        handlers.sort name: 'Text Elements', select: matchers.isContainerEl, priority: -1, {el1, el2 ->
            def v1 = matchers.isContainerEl(el1.el) ? 1 : -1;
            def v2 = matchers.isContainerEl(el2.el) ? 1 : -1;
            return v1 - v2
        }
    }

    def isoTextEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(isofunc.isoText(el))
            }
        }
    }

    def isoUrlEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(el.'gmd:Url'.text())
            }
        }
    }

    def isoCodeListEl = { el ->
        f.html {
            it.span('class': 'md-text') {
                dt(f.nodeLabel(el))
                dd(el['@codeListValue'].text())
            }
        }
    }

    def isoEntryEl = { el, childData ->
        if (!childData.isEmpty()) {
            return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: childData])
        }
        return null
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
                party.'gmd:contactInfo']
        def contactData = handlers.processElement( childrenToProcess )

        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData])
    }

    /**
     * el must be a parent of gmd:contactInfo or a gmd:ContactInfo
     */
    def contactInfoEl = {el ->
        def contact = el.name() == 'gmd:CI_Contact' ? el : el.'gmd:CI_Contact'
        def phone = contact.'gmd:phone'.'gmd:CI_Telephone'

        def childrenToProcess = [
                phone.'gmd:voice',
                phone.'gmd:facsimile',
                phone.'gmd:address'.'gmd:CI_Address']
        def contactData = handlers.processElement( childrenToProcess )

        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData])
    }

    /**
     * el must be a parent of gmd:CI_Address or a gmd:CI_Address
     */
    def addressEl = {el ->
        def addresses = el.name() == 'gmd:CI_Address'? el : el.'gmd:CI_Address'
        def contactData = []
        def text = commonHandlers.nonEmpty(isoTextEl)

        addresses.'gmd:deliveryPoint'.each {contactData.add(text(it))}

        contactData = contactData.findAll{it != null}
        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: contactData.join("\n")])
    }

}