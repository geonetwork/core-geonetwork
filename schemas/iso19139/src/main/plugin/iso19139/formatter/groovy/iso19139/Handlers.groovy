package iso19139

import org.fao.geonet.services.metadata.format.FormatType
import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.MapConfig

public class Handlers {
    protected org.fao.geonet.services.metadata.format.groovy.Handlers handlers;
    protected org.fao.geonet.services.metadata.format.groovy.Functions f
    protected Environment env
    Matchers matchers
    Functions isofunc
    common.Handlers commonHandlers
    List<String> packageViews
    String rootEl = 'gmd:MD_Metadata'

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        commonHandlers = new common.Handlers(handlers, f, env)
        isofunc = new Functions(handlers: handlers, f:f, env:env, commonHandlers: commonHandlers)
        matchers =  new Matchers(handlers: handlers, f:f, env:env)
        packageViews = [
                'gmd:identificationInfo', 'gmd:metadataMaintenance', 'gmd:metadataConstraints', 'gmd:spatialRepresentationInfo',
                'gmd:distributionInfo', 'gmd:applicationSchemaInfo', 'gmd:dataQualityInfo', 'gmd:portrayalCatalogueInfo',
                'gmd:contentInfo', 'gmd:metadataExtensionInfo', 'gmd:referenceSystemInfo', rootEl]
    }

    def addDefaultHandlers() {
        handlers.add name: 'Text Elements', select: matchers.isTextEl, isoTextEl
        handlers.add name: 'Simple Text Elements', select: matchers.isSimpleTextEl, isoSimpleTextEl
        handlers.add name: 'URL Elements', select: matchers.isUrlEl, isoUrlEl
        handlers.add name: 'Simple Elements', select: matchers.isBasicType, isoBasicType
        handlers.add name: 'Boolean Elements', select: matchers.isBooleanEl, isoBooleanEl
        handlers.add name: 'CodeList Elements', select: matchers.isCodeListEl, isoCodeListEl
        handlers.add name: 'Date Elements', select: matchers.isDateEl, dateEl
        handlers.add name: 'Format Elements',  select: matchers.isFormatEl, group: true, formatEls
        handlers.add name: 'Keyword Elements', select: 'gmd:descriptiveKeywords', group:true, keywordsEl
        handlers.add name: 'ResponsibleParty Elements', select: matchers.isRespParty, pointOfContactEl
        handlers.add name: 'Graphic Overview', select: 'gmd:graphicOverview', group: true, graphicOverviewEl
        handlers.add select: 'gmd:language', group: false, isoLanguageEl
        handlers.add select: 'gmd:onLine', group: true, onlineResourceEls
        handlers.add name: 'gmd:topicCategory', select: 'gmd:topicCategory', group: true, isoSimpleTextElGrouped

        handlers.skip name: "skip date parent element", select: matchers.hasDateChild, {it.children()}
        handlers.skip name: "skip codelist parent element", select: matchers.hasCodeListChild, {it.children()}
        handlers.skip name: "skip containers: " + matchers.skipContainers, select: matchers.isSkippedContainer, {it.children()}

        handlers.add select: 'gmd:locale', group: true, localeEls
        handlers.add 'gmd:CI_Date', ciDateEl
        handlers.add 'gmd:CI_Citation', citationEl
        handlers.add name: 'Root Element', select: matchers.isRoot, rootPackageEl

        handlers.add name: 'identificationInfo elements', select: {it.parent().name() == 'gmd:identificationInfo'}, commonHandlers.entryEl(f.&nodeLabel, {el -> 'gmd_identificationInfo'})
        handlers.add name: 'Container Elements', select: matchers.isContainerEl, priority: -1, commonHandlers.entryEl(f.&nodeLabel, addPackageViewClass)

        commonHandlers.addDefaultStartAndEndHandlers();
        addExtentHandlers()

        handlers.sort name: 'Text Elements', select: matchers.isContainerEl, priority: -1, sortContainerEl
    }

    def sortContainerEl = {el1, el2 ->
        def v1 = matchers.isContainerEl(el1) ? 1 : -1;
        def v2 = matchers.isContainerEl(el2) ? 1 : -1;
        return v1 - v2
    }
    def addPackageViewClass = {el -> if (packageViews.contains(el.name())) return el.name().replace(':', '_')}

    def addExtentHandlers() {
        handlers.add commonHandlers.matchers.hasChild('gmd:EX_Extent'), commonHandlers.flattenedEntryEl({it.'gmd:EX_Extent'}, f.&nodeLabel)
        handlers.add name: 'BBox Element', select: matchers.isBBox, bboxEl(false)
        handlers.add name: 'Polygon Element', select: matchers.isPolygon, polygonEl(false)
        handlers.add 'gmd:geographicElement', commonHandlers.processChildren{it.children()}
        handlers.add 'gmd:extentTypeCode', extentTypeCodeEl
    }

    def isoTextEl = { isofunc.isoTextEl(it, isofunc.isoText(it))}
    def isoUrlEl = { isofunc.isoTextEl(it, isofunc.isoUrlText(it))}
    def isoCodeListEl = {isofunc.isoTextEl(it, f.codelistValueLabel(it))}
    def isoBasicType = {isofunc.isoTextEl(it, it.'*'.text())}
    def isoSimpleTextEl = { isofunc.isoTextEl(it, it.text()) }
    def isoSimpleTextElGrouped = { elems ->
        def listItems = elems.findAll{!it.text().isEmpty()}.collect {it.text()};
        handlers.fileResult("html/list-entry.html", [label:f.nodeLabel(elems[0]), listItems: listItems])
    }
    def parseBool(text) {
        switch (text.trim().toLowerCase()){
            case "1":
            case "true":
            case "y":
                return true;
            default:
                return false;
        }
    }
    def isoBooleanEl = {isofunc.isoTextEl(it, parseBool(it.'*'.text()).toString())}
    def dateEl = {isofunc.isoTextEl(it, it.text());}
    def extentTypeCodeEl = {
        isofunc.isoTextEl(it, parseBool(it.text()) ? 'include' : 'excluded')
    }
    def ciDateEl = {
        if(!it.'gmd:date'.'gco:Date'.text().isEmpty()) {
            def dateType = f.codelistValueLabel(it.'gmd:dateType'.'gmd:CI_DateTypeCode')
            commonHandlers.func.textEl(dateType, it.'gmd:date'.'gco:Date'.text());
        }
    }

    def localeEls = { els ->
        def locales = []
        els.each {
            it.'gmd:PT_Locale'.each { loc ->
                locales << [
                        language: f.codelistValueLabel(loc.'gmd:languageCode'.'gmd:LanguageCode'),
                        charset: f.codelistValueLabel(loc.'gmd:characterEncoding'.'gmd:MD_CharacterSetCode')
                ]
            }
        }
        handlers.fileResult("html/locale.html", [
                label: f.nodeLabel(els[0]),
                locales: locales
        ])
    }
    def isoLanguageEl = { language ->
        def lang;
        if (!language.'gmd:LanguageCode'.isEmpty()) {
            lang = f.codelistValueLabel(language.'gmd:LanguageCode')
        } else {
            lang = f.translateLanguageCode(language.text());
        }

        commonHandlers.func.textEl(f.nodeLabel(language), lang);
    }
    def onlineResourceEls = { els ->
        def links = []

        els.each {it.'gmd:CI_OnlineResource'.each { link ->
                links << [
                        href : isofunc.isoUrlText(link.'gmd:linkage'),
                        name : isofunc.isoText(link.'gmd:name'),
                        desc : isofunc.isoText(link.'gmd:description')
                ]
            }
        }

        handlers.fileResult('html/online-resource.html', [
                label: f.nodeLabel(els[0]),
                links: links
        ])
    }

    def formatEls = { els ->
        def formats = []

        def resolveFormat = { el ->
            def format = el.'gmd:MD_Format'
            if (format.isEmpty()) {
                format = el
            }
            format
        }

        els.each {el ->
            def format = resolveFormat(el)
            def valueMap = [:]
            format.children().list().each {child ->
                if (child.name().equals("gmd:formatDistributor")) {
                    return;
                }
                String[] parts = child.name().split(":");
                String name;
                if (parts.length == 2) {
                    name = parts[1]
                } else {
                    name = parts[0]
                }

                valueMap.put(name, isofunc.isoText(child))
            }
            def distributor = resolveFormat(el).'gmd:formatDistributor'.'gmd:MD_Distributor'.'gmd:distributorContact'.'*'
            if (!distributor.text().isEmpty()) {
                valueMap.put('formatDistributor', handlers.processElements(distributor))
            }

            formats.add(valueMap)
        }

        def label = "format"
        if (!els.isEmpty()) {
            label = f.nodeLabel(els[0])
        }

        def model = [label: label, formats: formats]
        handlers.fileResult("html/format.html", model)
    }
    def keywordsEl = {keywords ->
        def keywordProps = com.google.common.collect.ArrayListMultimap.create()
        keywords.collectNested {it.'gmd:MD_Keywords'.'gmd:keyword'.list()}.flatten().each { k ->
            def thesaurusName = isofunc.isoText(k.parent().'gmd:thesaurusName'.'gmd:CI_Citation'.'gmd:title')

            if (thesaurusName.isEmpty()) {
                def keywordTypeCode = k.parent().'gmd:type'.'gmd:MD_KeywordTypeCode'
                if (!keywordTypeCode.isEmpty()) {
                    thesaurusName = f.codelistValueLabel(keywordTypeCode)
                }
            }

            if (thesaurusName.isEmpty()) {
                thesaurusName = f.translate("noThesaurusName")
            }
            keywordProps.put(thesaurusName, isofunc.isoText(k))
        }

        return handlers.fileResult('html/keyword.html', [
                label : f.nodeLabel("gmd:descriptiveKeywords", null),
                keywords: keywordProps.asMap()])
    }
    def isSmallImage(img) {
        return img.matches(".+_s\\.\\w+");
    }
    def graphicOverviewEl = {graphics ->
        def links = []
        def hasLargeGraphic = graphics.find {graphic ->
            def url = graphic.'gmd:fileName'.text()
            !(url.startsWith("http://") || url.startsWith("https://")) && !isSmallImage(url)
        }
        graphics.each {it.'gmd:MD_BrowseGraphic'.each { graphic ->
            def img = graphic.'gmd:fileName'.text()
            String thumbnailUrl;
            if (img.startsWith("http://") || img.startsWith("https://")) {
                thumbnailUrl = img;
            } else if (!isSmallImage(img) || !hasLargeGraphic) {
                thumbnailUrl = env.getLocalizedUrl() + "resources.get?fname=" + img + "&amp;access=public&amp;id=" + env.getMetadataId();
            }

            if (thumbnailUrl != null) {
                links << [
                        src : thumbnailUrl,
                        desc: isofunc.isoText(graphic.'gmd:fileDescription')
                ]
            }

        }}
        handlers.fileResult("html/graphic-overview.html", [
                label: f.nodeLabel(graphics[0]),
                graphics: links
        ])
    }
    def citationEl = { el ->
        Set processedChildren = ['gmd:title', 'gmd:alternateTitle', 'gmd:identifier', 'gmd:ISBN', 'gmd:ISSN',
                                 'gmd:date', 'gmd:edition', 'gmd:editionDate', 'gmd:presentationForm']

        def otherChildren = el.children().findAll { ch -> !processedChildren.contains(ch.name()) }

        def model = [
                title :  handlers.processElements([el.'gmd:title']),
                altTitle : handlers.processElements([el.'gmd:alternateTitle']),
                date : handlers.processElements(el.'gmd:date'.'gmd:CI_Date'),
                editionInfo: commonHandlers.func.textEl(el.'gmd:edition'.text(), el.'gmd:editionDate'.'gco:Date'.text()),
                identifier : isofunc.isoTextEl(el.'gmd:identifier', el.'gmd:identifier'.'*'.'gmd:code'.text()),
                presentationForm : isofunc.isoTextEl(el.'gmd:presentationForm', f.codelistValueLabel(el.'gmd:presentationForm'.'gmd:CI_PresentationFormCode')),
                ISBN : handlers.processElements(el.'gmd:ISBN'),
                ISSN : handlers.processElements(el.'gmd:ISSN'),
                otherData : handlers.processElements(otherChildren)
        ]
        return handlers.fileResult("html/citation.html", model)
    }

    /**
     * El must be a parent of gmd:CI_ResponsibleParty
     */
    def pointOfContactEl = { el ->

        def party = el.children().find { ch ->
            ch.name() == 'gmd:CI_ResponsibleParty' || ch['@gco:isoType'].text() == 'gmd:CI_ResponsibleParty'
        }

        def general = pointOfContactGeneralData(party);
        def groups = party.'gmd:contactInfo'.'*'.'*'

        def half = (int) Math.round((groups.size()) / 2)

        def output = '<div class="row">'
        output += commonHandlers.func.textColEl(general.toString() + handlers.processElements(groups.take(half - 1)), 6)
        output += commonHandlers.func.textColEl(handlers.processElements(groups.drop(half - 1)), 6)
        output += '</div>'

        return handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: output])
    }

    def pointOfContactGeneralData(party) {
        def generalChildren = [
                party.'gmd:individualName',
                party.'gmd:organisationName',
                party.'gmd:positionName',
                party.'gmd:role'
        ]
        handlers.fileResult('html/2-level-entry.html', [label: f.translate('general'), childData: handlers.processElements(generalChildren)])
    }

    def polygonEl(thumbnail) {
        return { el ->
            MapConfig mapConfig = env.mapConfiguration
            def mapproj = mapConfig.mapproj
            def background = mapConfig.background
            def width = thumbnail? mapConfig.thumbnailWidth : mapConfig.width
            def mdId = env.getMetadataId();
            def xpath = f.getXPathFrom(el);


            if (xpath != null) {
                def image = "<img src=\"region.getmap.png?mapsrs=$mapproj&amp;width=$width&amp;background=$background&amp;id=metadata:@id$mdId:@xpath$xpath\"\n" +
                        "         style=\"width:${width/4}; min-height:${width/4};\" />"
                handlers.fileResult('html/2-level-entry.html', [label: f.nodeLabel(el), childData: image])
            }
        }
    }

    def bboxEl(thumbnail) {
        return { el ->
            if (el.parent().'gmd:EX_BoundingPolygon'.text().isEmpty() &&
                    el.parent().parent().'gmd:geographicElement'.'gmd:EX_BoundingPolygon'.text().isEmpty()) {
                def replacements = bbox(thumbnail, el)
                replacements['label'] = f.nodeLabel(el)
                replacements['pdfOutput'] = env.formatType == FormatType.pdf

                handlers.fileResult("html/bbox.html", replacements)
            }
        }
    }

    def bbox(thumbnail, el) {
        def mapConfig = env.mapConfiguration
        if (thumbnail) {
            mapConfig.setWidth(mapConfig.thumbnailWidth)
        }

        return [ w: el.'gmd:westBoundLongitude'.'gco:Decimal'.text(),
          e: el.'gmd:eastBoundLongitude'.'gco:Decimal'.text(),
          s: el.'gmd:southBoundLatitude'.'gco:Decimal'.text(),
          n: el.'gmd:northBoundLatitude'.'gco:Decimal'.text(),
          geomproj: "EPSG:4326",
          minwidth: mapConfig.getWidth() / 4,
          minheight: mapConfig.getWidth() / 4,
          mapconfig: mapConfig
        ]
    }
    def rootPackageEl = {
        el ->
            def rootPackage = el.children().findAll { ch -> !this.packageViews.contains(ch.name()) }
            def otherPackage = el.children().findAll { ch -> this.packageViews.contains(ch.name()) }

            def rootPackageData = handlers.processElements(rootPackage, el);
            def otherPackageData = handlers.processElements(otherPackage, el);

            def rootPackageOutput = handlers.fileResult('html/2-level-entry.html',
                    [label: f.nodeLabel(el), childData: rootPackageData, name: rootEl.replace(":", "_")])

            return  rootPackageOutput.toString() + otherPackageData
    }
}
