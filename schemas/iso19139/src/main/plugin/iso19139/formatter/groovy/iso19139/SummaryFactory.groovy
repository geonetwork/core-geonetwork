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

        Environment env = isoHandler.env

        def linkBlockName = "hierarchy"
        if (env.formatType == FormatType.pdf) {
            StaticLinkBlock hierarchy = new StaticLinkBlock(linkBlockName)
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
            def placeholderId = "link-placeholder-" + linkBlockName
            def js = """
\$(function() {
  \$('${LinkBlock.CSS_CLASS_PREFIX + linkBlockName}').hide();
  \$.ajax('xml.relation?id=${env.metadataId}&type=$relatedTypes', {
    accepts:'application/json',
    success: function (data) {
      var types = {};

      \$.each(data.relation, function (rel) {
        var type = rel['@type'];
        \$.each(rel.metadata, function (md) {
          var uuid = md['geonet:info'].uuid;
          var title = md.title ? md.title : md.defaultTitle;
          if (!title) {
            title = uuid;
          }

          var url;
          if (uuid) {
            url = "javascript:open('md.format.html?xsl=full_view&amp;schema=iso19139&amp;uuid=' + encodeURIComponent(uuid), 'related');"
          } else {
            url = "javascript:alert('${isoHandler.f.translate("noUuidInLink")}');"
          }

          var obj = { url: url, title: title};

          if (title && uuid) {
            if (types.type) {
              types.type.push (obj);
            } else {
              types.type = [obj];
            }
          }
        });
      });

      var placeholder = \$('$placeholderId');

      \$.each(types, function (key, value) {
        var html = '<div class="col-xs-12" style="background-color: #F7EEE1;">' +
                   '  <img src="${isoHandler.env.localizedUrl + "../../images/"}' + key + '.png"/>';
        \$.each(value, function (rel) {
          html += '  <div class="col-xs-6 col-md-4"><a href="' + rel.url + '">' + rel.title + '</a></div>';
        });
        html += '</div>';
        placeholder.add(html);
      });

      \$('${LinkBlock.CSS_CLASS_PREFIX + linkBlockName}').show();
    },
    error: function (req, status, error) {
      \$('$placeholderId').add('<h3>Error loading related metadata</h3><p>' + error + '</p>');
      \$('${LinkBlock.CSS_CLASS_PREFIX + linkBlockName}').show();
    }
  })
});
"""
            def html = """
<script type="text/javascript">$js</script>
<div id="$placeholderId"> </div>
"""
            RawHtmlLinkBlock linkBlock = new RawHtmlLinkBlock(linkBlockName, html)
            summary.links.add(linkBlock)
        }


    }

    private static String createShowMetadataHref(isoHandler, String uuid) {
        if (uuid.trim().isEmpty()) {
            return "javascript:alert('" + isoHandler.f.translate("noUuidInLink") + "');"
        } else {
            return isoHandler.env.localizedUrl + "md.format.html?xsl=full_view&amp;schema=iso19139&amp;uuid=" + URLEncoder.encode(uuid, "UTF-8")
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
