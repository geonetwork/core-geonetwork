package iso19139

import org.fao.geonet.services.metadata.format.FormatType
import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.util.*
/**
 * Creates the {@link org.fao.geonet.services.metadata.format.groovy.util.Summary} instance for the iso19139 class.
 *
 * @author Jesse on 11/18/2014.
 */
class SummaryFactory {
    def isoHandlers;
    org.fao.geonet.services.metadata.format.groovy.Handlers handlers
    org.fao.geonet.services.metadata.format.groovy.Functions f
    Environment env

    def navBarItems

    SummaryFactory(isoHandlers) {
        this.isoHandlers = isoHandlers
        this.handlers = isoHandlers.handlers;
        this.f = isoHandlers.f;
        this.env = isoHandlers.env;
        this.navBarItems = ['gmd:identificationInfo', 'gmd:distributionInfo', isoHandlers.rootEl]
    }

    static void summaryHandler(select, isoHandler) {
        def factory = new SummaryFactory(isoHandler)
        factory.handlers.add name: "Summary Handler", select: select, {factory.create(it).getResult()}
    }

    Summary create(metadata) {

        Summary summary = new Summary(this.handlers, this.env, this.f)

        summary.title = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:citation'.'gmd:CI_Citation'.'gmd:title')
        summary.abstr = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract')

        configureKeywords(metadata, summary)
        configureFormats(metadata, summary)
        configureExtent(metadata, summary)
        configureThumbnails(metadata, summary)
        configureLinks(summary)

        if (env.formatType == FormatType.pdf || env.formatType == FormatType.testpdf) {
            summary.links.add(isoHandlers.commonHandlers.loadHierarchyLinkBlocks())
        } else {
            createDynamicHierarchyHtml(summary)
        }

        isoHandlers.commonHandlers.configureSummaryActionMenu(summary)

        def toNavBarItem = {s ->
            def name = f.nodeLabel(s, null)
            def abbrName = f.nodeTranslation(s, null, "abbrLabel")
            new NavBarItem(name, abbrName, '.' + s.replace(':', "_"))
        }
        summary.navBar = this.isoHandlers.packageViews.findAll{navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow = this.isoHandlers.packageViews.findAll{!navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow.add(isoHandlers.commonHandlers.createXmlNavBarItem())
        summary.content = this.isoHandlers.rootPackageEl(metadata)

        return summary
    }

    def configureKeywords(metadata, summary) {
        def keywords = metadata."**".findAll{it.name() == 'gmd:descriptiveKeywords'}
        if (!keywords.isEmpty()) {
            summary.keywords = this.isoHandlers.keywordsEl(keywords).toString()
        }
    }
    def configureFormats(metadata, summary) {
        def formats = metadata."**".findAll this.isoHandlers.matchers.isFormatEl
        if (!formats.isEmpty()) {
            summary.formats = this.isoHandlers.formatEls(formats).toString()
        }
    }
    def configureExtent(metadata, summary) {
        def extents = metadata."**".findAll { this.isoHandlers.matchers.isPolygon(it) || this.isoHandlers.matchers.isBBox(it) }
        def split = extents.split this.isoHandlers.matchers.isPolygon

        def polygons = split[0]
        def bboxes = split[1]

        def extent = ""
        if (!polygons.isEmpty()) {
            extent = this.isoHandlers.polygonEl(true)(polygons[0]).toString()
        } else if (!bboxes.isEmpty()) {
            extent = this.isoHandlers.bboxEl(true)(bboxes[0]).toString()
        }
        summary.extent = extent
    }

    def configureLinks(Summary summary) {
        Collection<String> links = this.env.indexInfo['link'];
        if (links != null && !links.isEmpty()) {
            LinkBlock linkBlock = new LinkBlock("links", "fa fa-link");
            summary.links.add(linkBlock)
            links.each { link ->
                def linkParts = link.split("\\|")
                def title = linkParts[0];
                def href = linkParts[2];
                def mimetype = linkParts[4].toLowerCase();
                if (title.trim().isEmpty()) {
                    title = href;
                }

                if (title != null && title.length() > 60) {
                    title = title.substring(0, 57) + "...";
                }

                def imagesDir = "../../images/formatter/"
                def type = "link";
                def icon = "";
                def iconClasses = "";
                if (mimetype.contains("kml")) {
                    type = "kml";
                    icon = imagesDir + "kml.png";
                } else if (mimetype.contains("OGC:")) {
                    type = "ogc";
                } else if (mimetype.contains("wms")) {
                    type = "wms";
                    icon = imagesDir + "wms.png";
                } else if (mimetype.contains("download")) {
                    type = "download";
                    iconClasses = "fa fa-download"
                } else if (mimetype.contains("link")) {
                    type = "link";
                    iconClasses = "fa fa-link"
                } else if (mimetype.contains("wfs")) {
                    type = "wfs";
                    icon = imagesDir + "wfs.png";
                }

                def linkType = new LinkType(type, icon, iconClasses)
                linkBlock.put(linkType, new Link(href, title))
            }
        }

    }

    void createDynamicHierarchyHtml(Summary summary) {
        def hierarchy = "hierarchy"

        def jsVars = [
                linkBlockClass: LinkBlock.CSS_CLASS_PREFIX + hierarchy,
                metadataId: this.env.metadataId
        ]
        def js = this.handlers.fileResult("js/dynamic-hierarchy.js", jsVars)
        def html = """
<script type="text/javascript">
//<![CDATA[
$js
//]]></script>
<div><i class="fa fa-circle-o-notch fa-spin"></i>&nbsp;Loading...</div>
"""

        LinkBlock linkBlock = new LinkBlock(hierarchy, "fa fa-code-fork")
        linkBlock.html = html
        summary.links.add(linkBlock)
    }

    private static void configureThumbnails(metadata, header) {
        def logos = metadata.'gmd:identificationInfo'.'*'.'gmd:graphicOverview'.'gmd:MD_BrowseGraphic'.'gmd:fileName'.'gco:CharacterString'

        logos.each { logo ->
            header.addThumbnail(logo.text())
        }
    }
}
