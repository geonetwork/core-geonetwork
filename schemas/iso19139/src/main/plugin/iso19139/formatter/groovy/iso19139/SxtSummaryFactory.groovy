package iso19139

import org.fao.geonet.api.records.formatters.FormatType
import org.fao.geonet.api.records.formatters.groovy.Environment
import org.fao.geonet.api.records.formatters.groovy.util.*
import org.fao.geonet.domain.ISODate
import java.text.SimpleDateFormat

/**
 * Creates the {@link org.fao.geonet.services.metadata.format.groovy.util.Summary} instance for the iso19139 class.
 *
 * @author Fgravin on 28/03/2015.
 */
class SxtSummaryFactory {
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
    summary.abstr = this.isoHandlers.isofunc.isoText(metadata.'gmd:identificationInfo'.'*'.'gmd:abstract').replaceAll("\n", "<br>")

    configureKeywords(metadata, summary)
    //configureFormats(metadata, summary)
    configureExtent(metadata, summary)
    configureThumbnails(metadata, summary)
    configureDataQualityInfo(metadata, summary)
    configureDates(metadata, summary)
    configureContacts(metadata, summary)
    configureConstraints(metadata, summary)
    configureNetworkLinkDescription(metadata, summary)
    configureDoi(metadata, summary)

    //createCollapsablePanel()

    summary.associated.add(isoHandlers.commonHandlers.loadHierarchyLinkBlocks())
    //summary.associated.add(createDynamicAssociatedHtml(summary))

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
    if (!keywords.isEmpty() && keywords.get(0)) {
      def ks = this.isoHandlers.keywordsElSxt(keywords)
      if(ks) summary.keywords = ks.toString()
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
    def contacts = metadata.'gmd:identificationInfo'."**".findAll{it.name() == 'gmd:CI_ResponsibleParty'}
      .sort{it.parent().name() == 'gmd:pointOfContact' ? -1 : 1}
    if (!contacts.isEmpty()) {
      summary.contacts = this.isoHandlers.contactsElSxt(contacts).toString()
    }
  }

  def configureNetworkLinkDescription(metadata, summary) {
    def networkLinks = metadata."**".findAll {
      it.name() == 'gmd:CI_OnlineResource' &&
        it.'gmd:protocol'.'gco:CharacterString' == 'NETWORK:LINK'
    }

    if (networkLinks.size() >= 1) {
      def networkLinksDescription = [];
      networkLinks.forEach { it ->
        def name = this.isoHandlers.isofunc.isoText(it.'gmd:name')
        def link = it.'gmd:linkage'.toString()
        def prettyName = name ? "$name : $link" : "$link"
        networkLinksDescription.push(
          isoHandlers.commonHandlers.func.urlToHtml(prettyName))
      }

      summary.networkLinksDescription = networkLinksDescription
    }
  }

  def configureConstraints(metadata, summary) {
    def constraints = metadata."**".findAll{it.name() == 'gmd:MD_LegalConstraints'}
    if (!constraints.isEmpty()) {
      summary.constraints = this.isoHandlers.constraintsElSxt(constraints).toString()
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
    def statementsElts = metadata."**".findAll{it.name() == 'gmd:statement'}
    def statementsString = []
    statementsElts.collectNested {metadata.'**'.findAll{it.name() == 'gmd:statement'}}.flatten().each { k ->
      statementsString.add(isoHandlers.commonHandlers.func.htmlNewLine(this.isoHandlers.isofunc.isoText(k)))
    }

    if (!statementsString.isEmpty() && statementsString.get(0)) {
      def statements = this.isoHandlers.dataQualityInfoElSxt(statementsString).toString();
      summary.formats = isoHandlers.commonHandlers.func.urlToHtml(statements)
    }
  }

  def getContactsByType(metadata, type) {
    return metadata."gmd:identificationInfo"."**".findAll{
      it.name() == 'gmd:CI_ResponsibleParty' &&
        (it.'gmd:role'.'gmd:CI_RoleCode'['@codeListValue'] == type)}
  }

  def containsString(list, value) {
    for(String str: list) {
      if(str.trim().contains(value))
        return true;
    }
    return false;
  }

  def configureDoi(metadata, summary) {
    def doiElts = metadata."**".findAll{
      it.name() == 'gmd:CI_OnlineResource' &&
        it.'gmd:protocol'.'gco:CharacterString' == 'WWW:LINK-1.0-http--metadata-URL'
    }
    def mainPublicationElts = metadata."**".findAll{
      it.name() == 'gmd:CI_OnlineResource' &&
        it.'gmd:protocol'.'gco:CharacterString' == 'WWW:LINK-1.0-http--publication-URL'
    }

    if(doiElts.size() < 1 && mainPublicationElts.size() < 1) {
      summary.citation = ''
      return
    }

    def mainPublicationDescription = null
    if (mainPublicationElts.size() >= 1) {
      mainPublicationDescription = []
      mainPublicationElts.forEach{it ->
        def text = it.'gmd:description'.'gco:CharacterString'.text()
        mainPublicationDescription.push(
          isoHandlers.commonHandlers.func.urlToHtml(text))
      }
    }

    def el = doiElts[0]

    // contacts: all author, if none all originators
    def contactElts = getContactsByType(metadata, 'author')

    if(contactElts.size() == 0 ) {
      contactElts = getContactsByType(metadata, 'originator')
    }
    def contacts = [];
    contactElts.forEach{it ->
      def name = it.'gmd:individualName'.'gco:CharacterString'.text()
      if(name == null || name == "") {
        name = it.'gmd:organisationName'.'gco:CharacterString'.text()
      }
      if(name != null && !name.equals('')) {
        if(!containsString(contacts, name)) {
          contacts.push(name)
        }
      }
    }

    def publishers = [];
    def publisherElts = getContactsByType(metadata, 'publisher')
    publisherElts.forEach{it ->
      def name = it.'gmd:organisationName'.'gco:CharacterString'.text()
      if(name != null && !name.equals('')) {
        if(!containsString(publishers, name)) {
          publishers.push(name)
        }
      }
    }
    publishers = publishers.join(',')
    if(!publishers.equals('')) {
      publishers += '.'
    }



    def dateElts = metadata.'gmd:identificationInfo'.'*'.'gmd:citation'."**".findAll{it.name() == 'gmd:CI_Date' &&
      it.'gmd:dateType'.'gmd:CI_DateTypeCode'['@codeListValue'] == 'publication'}

    if(dateElts.size() <= 0) {
      dateElts = metadata.'gmd:identificationInfo'.'*'.'gmd:citation'."**".findAll{it.name() == 'gmd:CI_Date' &&
              it.'gmd:dateType'.'gmd:CI_DateTypeCode'['@codeListValue'] == 'creation'}
    }

    def year = '';
    if(dateElts.size() > 0) {
      def sDate = dateElts[0].'gmd:date'.'gco:Date'.text();
      if(sDate == null || sDate == "") {
        sDate = dateElts[0].'gmd:date'.'gco:DateTime'.text();
      }
      if(sDate != null && sDate != "") {
        def pattern = "yyyy-MM-dd"
        if(sDate.size() == 7) {
          pattern = "yyyy-MM"
        } else if(sDate.size() == 4) {
          pattern = "yyyy"
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date = format.parse(sDate);
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        year = df.format(date);
      }
    }

    def url = ''
    if(el) {
      url = el.'gmd:linkage'.'gmd:URL'
    }

    def replacements
    replacements = [
      url: url,
      year: year,
      title: summary.title,
      citationPOTitle: this.f.translate('citationPOTitle'),
      citationPOContent: this.f.translate('citationPOContent'),
      contacts: String.join(', ', contacts),
      publishers: publishers,
      mainPublicationDescription: mainPublicationDescription
    ]

    summary.citation = this.handlers.fileResult("html/sxt-citation.html", replacements)
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
