package iso19139sxt

import org.fao.geonet.services.metadata.format.FormatType
import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.util.*

/**
 * Creates the {@link org.fao.geonet.services.metadata.format.groovy.util.Summary} instance for the iso19139 class.
 *
 * @author Fgravin on 28/03/2015.
 */
class SummaryFactory {
    def isoHandlers;
    org.fao.geonet.services.metadata.format.groovy.Handlers handlers
    org.fao.geonet.services.metadata.format.groovy.Functions f
    Environment env

    def navBarItems

    /*
     * This field can be set by the creator and provided a closure that will be passed the summary object.  The closure can
     * perform customization for its needs.
     */
    Closure<Summary> summaryCustomizer = null

    SummaryFactory(isoHandlers, summaryCustomizer) {
        this.isoHandlers = isoHandlers
        this.handlers = isoHandlers.handlers;
        this.f = isoHandlers.f;
        this.env = isoHandlers.env;
        this.navBarItems = []
        this.summaryCustomizer = summaryCustomizer;
    }
    SummaryFactory(isoHandlers) {
        this(isoHandlers, null)
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
        //configureFormats(metadata, summary)
        configureExtent(metadata, summary)
        configureThumbnails(metadata, summary)
        configureLinks(summary)
        configureDataQualityInfo(metadata, summary)

        def toNavBarItem = {s ->
            def name = f.nodeLabel(s, null)
            def abbrName = f.nodeTranslation(s, null, "abbrLabel")
            new NavBarItem(name, abbrName, '.' + s.replace(':', "_"))
        }

        summary.navBar = this.isoHandlers.packageViews.findAll{navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow = new  ArrayList<String>()
        summary.content = this.isoHandlers.rootPackageEl(metadata)

        if (summaryCustomizer != null) {
            summaryCustomizer(summary);
        }

        return summary
    }

    def configureKeywords(metadata, summary) {
        def keywords = metadata."**".findAll{it.name() == 'gmd:descriptiveKeywords'}
        if (!keywords.isEmpty()) {
            summary.keywords = this.isoHandlers.keywordsElSxt(keywords).toString()
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
            extent = this.isoHandlers.bboxElSxt(true)(bboxes[0]).toString()
        }
        summary.extent = extent
    }

    def configureDataQualityInfo(metadata, summary) {
        def statements = metadata."**".findAll{it.name() == 'gmd:statement'}
        if (!statements.isEmpty()) {
            summary.formats = this.isoHandlers.dataQualityInfoElSxt(statements).toString()
        }
    }

    def configureLinks(Summary summary) {
        Collection<String> links = this.env.indexInfo['link'];
        if (links != null && !links.isEmpty()) {
            LinkBlock linkBlock = new LinkBlock("links", "fa fa-link");
            summary.links.add(linkBlock)

            links.each { link ->
                def linkParts = link.split("\\|")
                def title = linkParts[0];
                def desc = linkParts[1];
                def href = linkParts[2];
                def protocol = linkParts[3].toLowerCase();
                if (title.trim().isEmpty()) {
                    title = desc;
                }
                if (title.trim().isEmpty()) {
                    title = href;
                }
                def linkClass = href.trim().isEmpty() ? 'text-muted' : '';

                def imagesDir = "../../images/formatter/"
                def type;
                def icon = "";
                def iconClasses = "";
                if (protocol.contains("kml")) {
                    type = "kml";
                    icon = imagesDir + "kml.png";
                } else if (protocol.contains("ogc:")) {
                    type = "ogc";
                } else if (protocol.contains("wms")) {
                    type = "wms";
                    icon = imagesDir + "wms.png";
                } else if (protocol.contains("download")) {
                    type = "download";
                    iconClasses = "fa fa-download"
                } else if (protocol.contains("wfs")) {
                    type = "wfs";
                    icon = imagesDir + "wfs.png";
                } else {
                    type = "link";
                    iconClasses = "fa fa-link"
                }

                def linkType = new LinkType(type, icon, iconClasses)
                linkBlock.put(linkType, new Link(href, title, linkClass))
            }
        }

    }

    LinkBlock createDynamicAssociatedHtml(Summary summary) {
        def associated = "associated-link"

        def html = """
<script type="text/javascript">
//<![CDATA[
    gnFormatter.loadAssociated('${LinkBlock.CSS_CLASS_PREFIX + associated}', '${this.env.metadataId}');
//]]></script>
<div><i class="fa fa-circle-o-notch fa-spin pad-right associated-spinner"></i>Loading...</div>
"""

        LinkBlock linkBlock = new LinkBlock(associated, "fa fa-sitemap")
        linkBlock.html = html
        return linkBlock;
    }

    private static void configureThumbnails(metadata, header) {
        def logos = metadata.'gmd:identificationInfo'.'*'.'gmd:graphicOverview'.'gmd:MD_BrowseGraphic'.'gmd:fileName'.'gco:CharacterString'

        logos.each { logo ->
            header.addThumbnail(logo.text())
        }
    }
}
