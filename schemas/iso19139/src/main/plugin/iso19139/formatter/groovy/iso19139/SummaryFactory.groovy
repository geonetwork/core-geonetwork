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
    static void summaryHandler(select, isoHandler) {
        isoHandler.handlers.add name: "Summary Handler", select: select, {create(it, isoHandler).getResult()}
    }
    static Summary create(metadata, isoHandler) {
        def handlers = isoHandler.handlers;
        def f = isoHandler.f;
        def env = isoHandler.env;

        Summary summary = new Summary(handlers, env, f)

        Collection<String> logo = env.indexInfo['_logo'];
        if (logo != null && !logo.isEmpty()) {
            summary.logo = env.localizedUrl + "../.." + logo.iterator().next()
        }
        summary.title = isoHandler.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:citation'.'gmd:CI_Citation'.'gmd:title')
        summary.abstr = isoHandler.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract')

        configureThumbnails(metadata, summary)
        configureLinks(isoHandler, summary)
        configureHierarchy(isoHandler, summary)

        def navBarItems = ['gmd:identificationInfo', 'gmd:distributionInfo', 'gmd:dataQualityInfo', 'gmd:spatialRepresentationInfo',
                           'gmd:metadataExtensionInfo', 'gmd:MD_Metadata']
        def toNavBarItem = {s ->
            def name = f.nodeLabel(s, null)
            new NavBarItem(name, s.replace(':', "_"))
        }
        summary.navBar = isoHandler.packageViews.findAll{navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow = isoHandler.packageViews.findAll{!navBarItems.contains(it)}.collect (toNavBarItem)

        summary.content = isoHandler.rootPackageEl(metadata)

        return summary
    }

    static def configureLinks(isoHandler, Summary summary) {
        def env = isoHandler.env
        Collection<String> links = env.indexInfo['link'];
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

    private static void configureHierarchy(isoHandler, Summary summary) {

        def relatedTypes = ["service","children","related","parent","dataset","fcat","siblings","associated","source","hassource"]
        def uuid = isoHandler.env.metadataUUID
        def id = isoHandler.env.metadataId

        Environment env = isoHandler.env

        def linkBlockName = "hierarchy"
        if (env.formatType == FormatType.pdf || env.formatType == FormatType.testpdf) {
            createStaticHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary, isoHandler)
        } else {
            createDynamicHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary, isoHandler, env)
        }


    }

    static void createDynamicHierarchyHtml(List<String> relatedTypes, String uuid, int id, String linkBlockName,
                                           Summary summary, isoHandler, Environment env) {
        def placeholderId = "link-placeholder-" + linkBlockName
        def typeTranslations = new StringBuilder()
        relatedTypes.eachWithIndex {type, i ->
            typeTranslations.append("\t'").append(type).append("': '").append(isoHandler.f.translate(type)).append('\'')
            if (i != relatedTypes.size() - 1) {
                typeTranslations.append(",\n");
            }
        }
        def jsVars = [
                typeTranslations: typeTranslations,
                metadataId: env.metadataId,
                relatedTypes: relatedTypes.join("|"),
                noUuidInLink: isoHandler.f.translate("noUuidInLink"),
                placeholderId: placeholderId,
                imagesDir: isoHandler.env.localizedUrl + "../../images/",
                linkBlockClass: LinkBlock.CSS_CLASS_PREFIX + linkBlockName
        ]
        def js = isoHandler.handlers.fileResult("js/dynamic-hierarchy.js", jsVars)
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

    static void createStaticHierarchyHtml(relatedTypes, uuid, id, linkBlockName, summary, isoHandler) {
        LinkBlock hierarchy = new LinkBlock(linkBlockName)
        summary.links.add(hierarchy);
        def bean = isoHandler.env.getBean(GetRelated.class)
        def related = bean.getRelated(ServiceContext.get(), id, uuid, relatedTypes.join("|"), 1, 1000, true)

        related.getChildren("relation").each {rel ->
            def type = rel.getAttributeValue("type")
            def icon = isoHandler.env.localizedUrl + "../../images/" + type + ".png";

            def linkType = new LinkType(type, icon)
            rel.getChildren("metadata").each {md ->
                def href = createShowMetadataHref(isoHandler, md.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid"))
                def title = md.getChildText("title")
                if (title != null) {
                    title = md.getChildText("defaultTitle")
                }
                hierarchy.put(linkType, new Link(href, title))
            }
        }
    }

    private static String createShowMetadataHref(isoHandler, String uuid) {
        if (uuid.trim().isEmpty()) {
            return "javascript:alert('" + isoHandler.f.translate("noUuidInLink") + "');"
        } else {
            return isoHandler.env.localizedUrl + "md.format.html?xsl=full_view&amp;schema=iso19139&amp;uuid=" + URLEncoder.encode(uuid, "UTF-8")
        }
    }

    private static void configureThumbnails(metadata, header) {
        def logos = metadata.'gmd:identificationInfo'.'*'.'gmd:graphicOverview'.'gmd:MD_BrowseGraphic'.'gmd:fileName'.'gco:CharacterString'

        logos.each { logo ->
            if (header.smallThumbnail == null && logo.text().contains("_s\\.")) {
                header.smallThumbnail = logo.text();
            } else if (header.largeThumbnail == null) {
                header.largeThumbnail = logo.text();
            }
        }
    }
}
