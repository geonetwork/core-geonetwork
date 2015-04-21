package iso19139

import org.fao.geonet.services.metadata.format.FormatType
import org.fao.geonet.services.metadata.format.groovy.Environment
import org.fao.geonet.services.metadata.format.groovy.util.*

/**
 * Creates the {@link org.fao.geonet.services.metadata.format.groovy.util.Summary} instance for the iso19139 class.
 *
 * @author Fgravin on 28/03/2015.
 */
class SxtSummaryFactory {
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

    SxtSummaryFactory(isoHandlers, summaryCustomizer) {
        this.isoHandlers = isoHandlers
        this.handlers = isoHandlers.handlers;
        this.f = isoHandlers.f;
        this.env = isoHandlers.env;
        this.navBarItems = []
        this.summaryCustomizer = summaryCustomizer;
    }
    SxtSummaryFactory(isoHandlers) {
        this(isoHandlers, null)
    }

    static void summaryHandler(select, isoHandler) {
        def factory = new SxtSummaryFactory(isoHandler)
        factory.handlers.add name: "Summary Handler", select: select, {factory.create(it).getResult()}
    }

    SxtSummary create(metadata) {

        SxtSummary summary = new SxtSummary(this.handlers, this.env, this.f)

        summary.title = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:citation'.'gmd:CI_Citation'.'gmd:title')
        summary.abstr = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract')

        configureKeywords(metadata, summary)
        //configureFormats(metadata, summary)
        configureExtent(metadata, summary)
        configureThumbnails(metadata, summary)
        configureDataQualityInfo(metadata, summary)
        configureDates(metadata, summary)
        configureContacts(metadata, summary)

        //createCollapsablePanel()

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
    def configureDates(metadata, summary) {
        def dates = metadata.'gmd:identificationInfo'.'*'.'gmd:citation'."**".findAll{it.name() == 'gmd:CI_Date'}
        if (!dates.isEmpty()) {
            summary.dates = this.isoHandlers.datesElSxt(dates).toString()
        }
    }

    def configureContacts(metadata, summary) {
        def contacts = metadata."**".findAll{it.name() == 'gmd:CI_ResponsibleParty'}
        if (!contacts.isEmpty()) {
            summary.contacts = this.isoHandlers.contactsElSxt(contacts).toString()
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

    def createCollapsablePanel() {

/*
        def js = this.handlers.fileResult("js/utils.js", null)
        def htmlOrXmlEnd = {
            def required = """
            <script type="text/javascript">
            //<![CDATA[
                gnFormatter.formatterOnComplete();

            $js
                //]]></script>
            """
        }
        handlers.end htmlOrXmlEnd
*/
    }

    private static void configureThumbnails(metadata, header) {
        def logos = metadata.'gmd:identificationInfo'.'*'.'gmd:graphicOverview'.'gmd:MD_BrowseGraphic'.'gmd:fileName'.'gco:CharacterString'

        logos.each { logo ->
            header.addThumbnail(logo.text())
        }
    }
}
