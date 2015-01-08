package iso19139
import jeeves.server.context.ServiceContext
import org.fao.geonet.constants.Geonet
import org.fao.geonet.guiservices.metadata.GetRelated
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
        configureHierarchy(summary)

        configureActionMenu(summary)
        def toNavBarItem = {s ->
            def name = f.nodeLabel(s, null)
            def abbrName = f.nodeTranslation(s, null, "abbrLabel")
            new NavBarItem(name, abbrName, '.' + s.replace(':', "_"))
        }
        summary.navBar = this.isoHandlers.packageViews.findAll{navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow = this.isoHandlers.packageViews.findAll{!navBarItems.contains(it)}.collect (toNavBarItem)

        summary.content = this.isoHandlers.rootPackageEl(metadata)

        return summary
    }

    private void configureActionMenu(Summary summary) {
        def url = env.localizedUrl
        summary.actions << new MenuAction(label: "xml", javascript: "window.open('xml.metadata.get?uuid=${this.env.metadataUUID}')", iconClasses: "fa fa-code")

        if (env.canEdit()) {
            summary.actions << new MenuAction(label: "edit", javascript: "window.open('catalog.edit#/metadata/${this.env.metadataId}')", iconClasses: "fa fa-edit")
            def publishUrl = {
                def on = it ? "on" : "off"
                "md.privileges.update?update=true&id=${env.metadataId}&_1_0=$on&_1_1=$on&_1_5=$on&_1_6=$on"
            }

            def basicPublicJs = { isPublish ->
                """\$.ajax({
                        url: '${publishUrl(isPublish)}',
                        success: function() {
                            \$('li#menu-action-publish').toggleClass('disabled');
                            \$('li#menu-action-unpublish').toggleClass('disabled');
                        }
                      })""".replaceAll(/\s+/, " ")
            }


            def published = env.indexInfo.get("_groupPublished").contains("all") || env.indexInfo.get("_groupPublished").contains("guest")
            def publishAction = new MenuAction(label: "publish", javascript: basicPublicJs(true), iconClasses: "fa fa-unlock", liClasses: "disabled")
            summary.actions << publishAction
            if (!published && env.indexInfo.get("_valid").contains("1")) {
                publishAction.liClasses = ""
            }
            def unpublishAction = new MenuAction(label: "unpublish", javascript: basicPublicJs(false), iconClasses: "fa fa-lock", liClasses: "disabled")
            summary.actions << unpublishAction
            if (published) {
                unpublishAction.liClasses = ""
            }
        }
        summary.actions << new MenuAction(label: "export", iconClasses: "fa fa-share-alt", submenu: [
                new MenuAction(label: "exportRdf", javascript: "window.location.href = 'rdf.metadata.get?uuid=${this.env.metadataUUID}'", iconClasses: "fa fa-rss"),
                new MenuAction(label: "exportPdf", javascript: "window.open('md.format.pdf?xsl=full_view&uuid=${this.env.metadataUUID}')", iconClasses: "fa fa-print"),
                new MenuAction(label: "exportZip", javascript: "window.location.href = 'mef.export?version=2&uuid=${this.env.metadataUUID}'", iconClasses: "fa fa-archive")
        ]);

        def shareURL = { it + URLEncoder.encode("${url}md.format.html?xsl=full_view&uuid=${this.env.metadataUUID}", "UTF-8") }
        summary.actions << new MenuAction(label: "share", iconClasses: "fa fa-share", submenu: [
                new MenuAction(label: "googlePlus", javascript: "window.open('${shareURL('https://plus.google.com/share?url=')}')", iconClasses: "fa fa-google-plus"),
                new MenuAction(label: "twitter", javascript: "window.open('${shareURL('https://twitter.com/share?url=')}')", iconClasses: "fa fa-twitter"),
                new MenuAction(label: "facebook", javascript: "window.open('${shareURL('href="https://www.facebook.com/sharer.php?u=')}')", iconClasses: "fa fa-facebook")
        ]);
    }

    def configureKeywords(metadata, summary) {
        def keywords = metadata."**".findAll{it.name() == 'gmd:descriptiveKeywords'}
        summary.keywords = this.isoHandlers.keywordsEl(keywords).toString()
    }
    def configureFormats(metadata, summary) {
        def formats = metadata."**".findAll this.isoHandlers.matchers.isFormatEl
        summary.formats = this.isoHandlers.formatEls(formats).toString()
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
            LinkBlock linkBlock = new LinkBlock("links");
            summary.links.add(linkBlock)
            links.each { link ->
                def linkParts = link.split("\\|")
                def title = linkParts[0];
                def href = linkParts[2];
                def mimetype = linkParts[4].toLowerCase();
                if (title.trim().isEmpty()) {
                    title = href;
                }

                def type = "link";
                if (mimetype.contains("kml")) {
                    type = "kml";
                } else if (mimetype.contains("OGC:")) {
                    type = "ogc";
                } else if (mimetype.contains("wms")) {
                    type = "wms";
                } else if (mimetype.contains("download")) {
                    type = "download";
                } else if (mimetype.contains("link")) {
                    type = "link";
                } else if (mimetype.contains("wfs")) {
                    type = "wfs";
                }
                if (!(env.formatType == FormatType.pdf || env.formatType == FormatType.testpdf)) {
                    href = "javascript:window.open('${href.replace("'", "\\'")}', '${env.metadataUUID.replace('\'', '_')}_link')"
                }
                def linkType = new LinkType(type, null)
                linkBlock.put(linkType, new Link(href, title))
            }
        }

    }

    private void configureHierarchy(Summary summary) {

        def relatedTypes = ["service","children","related","parent","dataset","fcat","siblings","associated","source","hassource"]
        def uuid = this.env.metadataUUID
        def id = this.env.metadataId

        def linkBlockName = "hierarchy"
        if (this.env.formatType == FormatType.pdf || this.env.formatType == FormatType.testpdf) {
            createStaticHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary)
        } else {
            createDynamicHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary)
        }


    }

    void createDynamicHierarchyHtml(List<String> relatedTypes, String uuid, int id, String linkBlockName, Summary summary) {
        def placeholderId = "link-placeholder-" + linkBlockName
        def typeTranslations = new StringBuilder()
        relatedTypes.eachWithIndex {type, i ->
            typeTranslations.append("\t'").append(type).append("': '").append(this.isoHandlers.f.translate(type)).append('\'')
            if (i != relatedTypes.size() - 1) {
                typeTranslations.append(",\n");
            }
        }
        def jsVars = [
                typeTranslations: typeTranslations,
                metadataId: this.env.metadataId,
                relatedTypes: relatedTypes.join("|"),
                noUuidInLink: this.f.translate("noUuidInLink"),
                placeholderId: placeholderId,
                imagesDir: this.env.localizedUrl + "../../images/",
                linkBlockClass: LinkBlock.CSS_CLASS_PREFIX + linkBlockName
        ]
        def js = this.handlers.fileResult("js/dynamic-hierarchy.js", jsVars)
        def html = """
<script type="text/javascript">
//<![CDATA[
$js
//]]></script>
<div id="$placeholderId"> </div>
"""
        LinkBlock linkBlock = new LinkBlock(linkBlockName)
        linkBlock.html = html
        summary.links.add(linkBlock)
    }

    void createStaticHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary) {
        LinkBlock hierarchy = new LinkBlock(linkBlockName)
        summary.links.add(hierarchy);
        def bean = this.isoHandlers.env.getBean(GetRelated.class)
        def related = bean.getRelated(ServiceContext.get(), id, uuid, relatedTypes.join("|"), 1, 1000, true)

        related.getChildren("relation").each {rel ->
            def type = rel.getAttributeValue("type")
            def icon = this.isoHandlers.env.localizedUrl + "../../images/" + type + ".png";

            def linkType = new LinkType(type, icon)
            rel.getChildren("metadata").each {md ->
                def href = createShowMetadataHref(md.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid"))
                def title = md.getChildText("title")
                if (title != null) {
                    title = md.getChildText("defaultTitle")
                }
                hierarchy.put(linkType, new Link(href, title))
            }
        }
    }

    private String createShowMetadataHref(String uuid) {
        if (uuid.trim().isEmpty()) {
            return "javascript:alert('" + this.f.translate("noUuidInLink") + "');"
        } else {
            return this.env.localizedUrl + "md.format.html?xsl=full_view&amp;schema=iso19139&amp;uuid=" + URLEncoder.encode(uuid, "UTF-8")
        }
    }

    private static void configureThumbnails(metadata, header) {
        def logos = metadata.'gmd:identificationInfo'.'*'.'gmd:graphicOverview'.'gmd:MD_BrowseGraphic'.'gmd:fileName'.'gco:CharacterString'

        logos.each { logo ->
            header.addThumbnail(logo.text())
        }
    }
}
