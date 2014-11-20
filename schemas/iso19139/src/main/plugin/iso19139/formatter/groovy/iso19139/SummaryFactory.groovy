package iso19139

import jeeves.server.context.ServiceContext
import org.fao.geonet.constants.Geonet
import org.fao.geonet.guiservices.metadata.GetRelated
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

        summary.title = isoHandler.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:citation'.'gmd:CI_Citation'.'gmd:title')
        summary.abstr = isoHandler.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract')

        configureLogos(metadata, summary)
        configureHierarchy(isoHandler, summary)

        summary.navBar = '''
            <ul class="nav nav-pills">
              <li><a href="" rel=".gmd_identificationInfo">Identification</a></li>
              <li><a href="" rel=".gmd_distributionInfo" >Distribution</a></li>
              <li><a href="" rel=".gmd_dataQualityInfo" >Quality</a></li>
              <li><a href="" rel=".gmd_spatialRepresentationInfo" >Spatial rep.</a></li>
              <li><a href="" rel=".gmd_referenceSystemInfo" >Ref. system</a></li>
              <li><a href="" rel=".gmd_metadataExtensionInfo" >Extension</a></li>
              <li><a href="" rel=".gmd_MD_Metadata">Metadata</a></li>
              <li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="" title="More information"><i class="fa fa-ellipsis-h"></i><b class="caret"></b></a><ul class="dropdown-menu">
                    <li><a href="" rel=".gmd_contentInfo">Content</a></li>
                    <li><a href="" rel=".gmd_portrayalCatalogueInfo">Portrayal</a></li>
                    <li><a href="" rel=".gmd_metadataConstraints">Md. constraints</a></li>
                    <li><a href="" rel=".gmd_metadataMaintenance">Md. maintenance</a></li>
                    <li><a href="" rel=".gmd_applicationSchemaInfo">Schema info</a></li>
                 </ul>
              </li>
           </ul>
        '''

        summary.content = isoHandler.rootPackageEl(metadata)

        return summary
    }

    private static void configureHierarchy(isoHandler, Summary summary) {

        def relatedTypes = "service|children|related|parent|dataset|fcat|siblings|associated|source|hassource";
        def uuid = isoHandler.env.metadataUUID
        def id = isoHandler.env.metadataId

        if (isoHandler.env.param('print').toBool()) {
            StaticLinkBlock hierarchy = new StaticLinkBlock("hierarchy")
            summary.links.add(hierarchy);
            def bean = isoHandler.env.getBean(GetRelated.class)
            def related = bean.getRelated(ServiceContext.get(), id, uuid, relatedTypes, 1, 1000, true)

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
                    hierarchy.links.put(linkType, new Link(href, title))
                }
            }
        } else {
            RawHtmlLinkBlock linkBlock = new RawHtmlLinkBlock("hierarchy", "")
        }


    }

    private static String createShowMetadataHref(isoHandler, String uuid) {
        if (uuid.trim().isEmpty()) {
            return "javascript:alert('" + isoHandler.f.translate("noUuidInLink") + "');"
        } else {
            return "javascript:open('md.format.html?xsl=full_view&amp;schema=iso19139&amp;uuid=" + URLEncoder.encode(uuid, "UTF-8") + "', 'related');"
        }
    }

    private static void configureLogos(metadata, header) {
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
