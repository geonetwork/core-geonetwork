package common
import jeeves.server.context.ServiceContext
import org.fao.geonet.constants.Geonet
import org.fao.geonet.guiservices.metadata.GetRelated
import org.fao.geonet.kernel.GeonetworkDataDirectory
import org.fao.geonet.api.records.formatters.FormatType
import org.fao.geonet.api.records.formatters.groovy.Environment
import org.fao.geonet.api.records.formatters.groovy.util.AssociatedLink
import org.fao.geonet.api.records.formatters.groovy.util.Direction
import org.fao.geonet.api.records.formatters.groovy.util.LinkBlock
import org.fao.geonet.api.records.formatters.groovy.util.LinkType
import org.fao.geonet.api.records.formatters.groovy.util.NavBarItem
import org.fao.geonet.utils.Xml
import org.jdom.Element

public class Handlers {
    private org.fao.geonet.api.records.formatters.groovy.Handlers handlers;
    private org.fao.geonet.api.records.formatters.groovy.Functions f
    private Environment env

    common.Matchers matchers
    common.Functions func
    boolean requireValidMetadataForPublish = false;

    public Handlers(handlers, f, env) {
        this.handlers = handlers
        this.f = f
        this.env = env
        func = new common.Functions(handlers: handlers, f:f, env:env)
        matchers =  new common.Matchers(handlers: handlers, f:f, env:env)
    }

    def addDefaultStartAndEndHandlers() {
        handlers.start htmlOrXmlStart
        handlers.end htmlOrXmlEnd
    }

    def entryEl(labeller) {
        return entryEl(labeller, null)
    }
    /**
     * Creates a function that will process all children and sort then according to the sorter that applies to the elements. Then
     * returns the default html for the container elements.
     *
     * @param labeller a function for creating a label from the element
     * @param classer a function taking the element class(es) to add to the entry element.  The method should return a string.
     */
    def entryEl(labeller, classer) {
        return { el ->
            def childData = handlers.processElements(el.children(), el);
            def replacement = [label: labeller(el), childData: childData, name:'']

            if (classer != null) {
                replacement.name = classer(el);
            }

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', replacement)
            }
            return null
        }
    }
    def processChildren(childSelector) {
        return {el ->
            handlers.processElements(childSelector(el), el);
        }
    }
    /**
     * Creates a function which will:
     *
     * 1. Select a single element using the selector function
     * 2. Process all children of the element selected in step 1 with sorter that applies to the element selected in step 1
     * 3. Create a label using executing the labeller on the element passed to handler functions (not element selected in step 1)
     *
     * @param selector a function that will select a single element from the descendants of the element passed to it
     * @param labeller a function for creating a label from the element
     */
    def flattenedEntryEl(selector, labeller) {
        return { parentEl ->
            def el = selector(parentEl)
            def childData = handlers.processElements(el.children(), el);

            if (!childData.isEmpty()) {
                return handlers.fileResult('html/2-level-entry.html', [label: labeller(el), childData: childData])
            }
            return null
        }
    }

    def selectIsotype(name) {
        return {
            it.children().find { ch ->
                ch.name() == name || ch['@gco:isoType'].text() == name
            }
        }
    }


    def htmlOrXmlStart = {
        if (func.isHtmlOutput()) {
            def minimize = ''
            def baseUrl = func.f.fparams.url;
            if (env.param("debug").toBool()) {
                minimize = '?minimize=false'
            }
            String cssLinks = """
    <link rel="stylesheet" href="$baseUrl../../static/gn_bootstrap.css$minimize"/>
    <link rel="stylesheet" href="$baseUrl../../static/gn_metadata.css$minimize"/>""";

            if (func.isPDFOutput()) {
                cssLinks = """<link rel="stylesheet" href="$baseUrl../../static/gn_metadata_pdf.css$minimize"/>"""
            }
            return """
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8"/>
    $cssLinks
    <script src="$baseUrl../../static/lib.js$minimize"></script>
    <script src="$baseUrl../../static/gn_formatter_lib.js$minimize"></script>
</head>
<body>
"""
        } else {
            return ''
        }
    }

    def htmlOrXmlEnd = {
        def required = "";
        if (!func.isPDFOutput()) {
            required = """
<script type="text/javascript">
//<![CDATA[
    gnFormatter.formatterOnComplete();
//]]>
</script>"""
        }
        if (func.isHtmlOutput()) {
            return required + '</body></html>'
        } else {
            return required
        }
    }


    NavBarItem createXmlNavBarItem() {
        return new NavBarItem(f.translate("xml"), f.translate("xml"), "", "xml.metadata.get?uuid=${env.metadataUUID}")
    }

    def loadHierarchyLinkBlocks() {
        def uuid = this.env.metadataUUID
        def id = this.env.metadataId

        LinkBlock hierarchy = new LinkBlock("associated-link", "fa fa-sitemap")
        Element related = getRelatedReport(id, uuid)

        related.getChildren("relation").each { rel ->
            def type = rel.getAttributeValue("type")
            def direction = Direction.CHILD
            def association = rel.getAttributeValue("association")
            if (type == "sibling") {
                if (association != null && association != '') {
                    type = association;
                    direction = Direction.PARENT
                }
            } else if (type == "services" || type == "sources" || type == "parent" || type == "fcats") {
                direction = Direction.PARENT
            } else if( type == 'associated') {
                Element aggIndexEl = rel.getChildren().find{it.getName() startsWith "agg_"}
                if (aggIndexEl != null) {
                    type = aggIndexEl.name.substring(4)
                }
            }

            def relatedIdInfo = addRelation(hierarchy, uuid, rel, type, direction)

            if (relatedIdInfo != null && direction == Direction.PARENT) {
                def parentUUID = relatedIdInfo['uuid'] as String
                def report = getRelatedReport(relatedIdInfo['id'] as int, parentUUID)
                report.getChildren("relation").each { potentialSiblingRel ->
                     def relType = potentialSiblingRel.getAttributeValue("type")
                    if (association != null) {
                        boolean isAggSibling = potentialSiblingRel.getChildren("agg_$association").any {
                            it.getTextTrim() == parentUUID
                        }
                        if (isAggSibling) {
                            addRelation(hierarchy, parentUUID, potentialSiblingRel, association, Direction.SIBLING)
                        }
                    } else if (relType == 'datasets' || relType == 'hassource' || relType == 'hasfeaturecat') {
                        addRelation(hierarchy, parentUUID, potentialSiblingRel, relType, Direction.SIBLING)
                    } else if (relType == 'children') {
                        addRelation(hierarchy, parentUUID, potentialSiblingRel, "siblings", Direction.SIBLING)
                    }

                }
            }
        }

        return hierarchy;
    }

    private Element getRelatedReport(int id, String uuid) {
        def getRelatedBean = this.env.getBean(GetRelated.class)
        def relatedXsl = this.env.getBean(GeonetworkDataDirectory).getWebappDir().resolve("xslt/services/metadata/relation.xsl");
        def raw = getRelatedBean.getRelated(ServiceContext.get(), id, uuid, "", 1, 1000, true)
        def withGui = new Element("root").addContent(Arrays.asList(
                new Element("gui").addContent(Arrays.asList(
                        new Element("language").setText(env.lang3),
                        new Element("locUrl").setText(env.getLocalizedUrl())
                )),
                raw));
        def related = Xml.transform(withGui, relatedXsl);
        related
    }

    private Map addRelation(hierarchy, uuid, rel, type, direction) {
        def arrow;
        switch (direction) {
            case Direction.CHILD:
                arrow = "pad-left fa-long-arrow-down"
                break
            case Direction.PARENT:
                arrow = "pad-left fa-long-arrow-up"
                break
            default:
                arrow = "fa-arrows-h"
        }
        def linkType = new LinkType()
        linkType.name = type
        linkType.relationDirection = direction
        linkType.iconHtml = """
  <i class="fa ${arrow}" title="${f.translate(direction.name().toLowerCase() + "-plural")}"></i>
"""
        def md = rel.getChild("metadata")

        def mdEl, relUuid, relId;

        def relInfo = rel.getChild("info", Geonet.Namespaces.GEONET)

        if (md != null) {
            relUuid = md.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid")
            relId = md.getChild("info", Geonet.Namespaces.GEONET).getChildText("id")
            mdEl = md;
        } else if (rel.getChild("info", Geonet.Namespaces.GEONET) != null && relInfo.getChildText("uuid") != null) {
            relUuid = relInfo.getChildText("uuid")
            relId = relInfo.getChildText("id")
            mdEl = rel;
        } else {
            relUuid = rel.getChildText("uuid")
            relId = rel.getChildText("id")
            mdEl = rel
        }

        if (relUuid != null && env.metadataUUID != relUuid) {
            def href = createShowMetadataHref(relUuid)
            def title = mdEl.getChildText("title")
            if (title == null || title.isEmpty()) {
                title = mdEl.getChildText("defaultTitle")
            }

            if (title == null || title.isEmpty()) {
                title = relUuid;
            }

            def cls = uuid.trim().isEmpty() ? "text-muted" : ''

            def link = new AssociatedLink(href, title, cls)
            link.setAbstract(rel.getChildText('abstract'));
            link.setLogo(rel.getChildText('logo'));
            link.metadataId = relUuid;

            hierarchy.put(linkType, link)

            return ['uuid': relUuid, 'id' : relId]
        }
        return null;
    }

    private String createShowMetadataHref(String uuid) {
        if (uuid.trim().isEmpty()) {
            return "javascript:alert('" + this.f.translate("noUuidInLink") + "');"
        } else {
            return this.env.localizedUrl + "md.viewer#/full_view/" + URLEncoder.encode(uuid, "UTF-8")
        }
    }

}
