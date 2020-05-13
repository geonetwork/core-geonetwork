/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package iso19139
import org.fao.geonet.api.records.formatters.FormatType
import org.fao.geonet.api.records.formatters.groovy.Environment
import org.fao.geonet.api.records.formatters.groovy.util.*

import java.util.regex.Pattern

/**
 * Creates the {@link org.fao.geonet.api.records.formatters.groovy.util.Summary} instance for the iso19139 class.
 *
 * @author Jesse on 11/18/2014.
 */
class SummaryFactory {
    def isoHandlers;
    org.fao.geonet.api.records.formatters.groovy.Handlers handlers
    org.fao.geonet.api.records.formatters.groovy.Functions f
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
        this.navBarItems = ['gmd:identificationInfo', 'gmd:distributionInfo', isoHandlers.rootEl]
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
        summary.abstr = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract').replaceAll("\n", "<br>")

        configureKeywords(metadata, summary)
        configureFormats(metadata, summary)
        configureExtent(metadata, summary)
        configureThumbnails(metadata, summary)


        LinkBlock linkBlock = new LinkBlock('links', "fa fa-link");
        configureLinks(linkBlock, 'link', false, {
            def linkParts = it.split("\\|")
            [
                    title   : isoHandlers.isofunc.clean(linkParts[0]),
                    desc    : isoHandlers.isofunc.clean(linkParts[1]),
                    href    : isoHandlers.isofunc.clean(linkParts[2]),
                    protocol: isoHandlers.isofunc.clean(linkParts[3])
            ]
        })

        if (!linkBlock.links.isEmpty()) {
            summary.links.add(linkBlock)
        }

        /*
         * TODO fix the xslt transform required by loadHierarchyLinkBlocks when running tests.
         */
        if (env.formatType == FormatType.pdf/* || env.formatType == FormatType.testpdf */) {
            summary.associated.add(isoHandlers.commonHandlers.loadHierarchyLinkBlocks())
        } else {
            summary.associated.add(createDynamicAssociatedHtml(summary))
        }

        def toNavBarItem = {s ->
            def name = f.nodeLabel(s, null)
            def abbrName = f.nodeTranslation(s, null, "abbrLabel")
            new NavBarItem(name, abbrName, '.' + s.replace(':', "_"))
        }
        summary.navBar = this.isoHandlers.packageViews.findAll{navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow = this.isoHandlers.packageViews.findAll{!navBarItems.contains(it)}.collect (toNavBarItem)
        summary.navBarOverflow.add(isoHandlers.commonHandlers.createXmlNavBarItem())
        summary.content = this.isoHandlers.rootPackageEl(metadata)

        if (summaryCustomizer != null) {
            summaryCustomizer(summary);
        }

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

    def configureLinks(linkBlock, indexKey, urlAndTextEquals, objParser) {
        Collection<String> links = this.env.indexInfo[indexKey];
        if (links != null && !links.isEmpty()) {

            links.each { link ->
                def linkParts = objParser(link)
                def title = linkParts.title
                def desc = linkParts.desc
                def href = linkParts.href
                if (title.isEmpty()) {
                    title = desc;
                }
                if (title.isEmpty()) {
                    title = href;
                }

                if (href != '') {
                    def protocol = linkParts.protocol != null ? linkParts.protocol.toLowerCase() : '';
                    def linkClass = href.isEmpty() ? 'text-muted' : '';

                    def imagesDir = "../../images/formatter/"
                    def type;
                    def icon = "";
                    def iconClasses = "";
                    if (protocol.contains("kml")) {
                        type = "kml";
                        icon = imagesDir + "kml.png";
                    } else if (protocol.contains("wms")) {
                        type = "wms";
                        icon = imagesDir + "wms.png";
                    } else if (protocol.contains("download")) {
                        type = "download";
                        iconClasses = "fa fa-download"
                    } else if (protocol.contains("wfs")) {
                        type = "wfs";
                        icon = imagesDir + "wfs.png";
                    } else if (protocol.contains("ogc:")) {
                        type = "ogc";
                    } else {
                        if (indexKey == 'wms_uri' ) {
                            type = "wms";
                            icon = imagesDir + "wms.png";
                        } else {
                            type = "link";
                            iconClasses = "fa fa-link"
                        }
                    }

                    def linkType = new LinkType(type, null, icon, iconClasses)

                    def linkObj = new Link(href, title, linkClass)
                    if (urlAndTextEquals) {
                        linkBlock.linkMap.put(linkType, linkObj);
                    } else {
                        linkBlock.put(linkType, linkObj)
                    }
                }
            }
        }
    }

    LinkBlock createDynamicAssociatedHtml(Summary summary) {
        def associated = "associated-link"
        def html = """
<script type="text/javascript">
//<![CDATA[
gnFormatter.loadAssociated(undefined, '.${LinkBlock.CSS_CLASS_PREFIX + associated}', '${this.env.metadataUUID}', undefined, '.associated-spinner')
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
