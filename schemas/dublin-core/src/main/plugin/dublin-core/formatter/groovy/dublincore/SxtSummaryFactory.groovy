package dublincore

import iso19139.SxtSummary
import org.fao.geonet.api.records.formatters.groovy.Environment

class SxtSummaryFactory {
  def dcHandlers

  org.fao.geonet.api.records.formatters.groovy.Handlers handlers
  org.fao.geonet.api.records.formatters.groovy.Functions f
  Environment env

  SxtSummaryFactory(dcHandlers, select) {
    this.dcHandlers = dcHandlers
    this.handlers = dcHandlers.handlers
    this.f = dcHandlers.f
    this.env = dcHandlers.env;
    this.handlers.add name: "Summary Handler", select: select, {
      this.create(it).getResult()}
  }

  SxtSummary create(metadata) {

    SxtSummary summary = new SxtSummary(this.handlers, this.env, this.f)

    summary.title = metadata.'dc:title'.text()
    summary.abstr = metadata.'dc:description'.text().replaceAll("\n", "<br>")

    summary.keywords = keywordsElSxt(metadata)
    summary.extent = bboxElSxt(metadata)
    summary.thumbnails = thumbnailsElSxt(metadata)
    summary.dates = datesElSxt(metadata)
    summary.contacts = contactsElSxt(metadata).toString()
    summary.constraints = constraintsElSxt(metadata).toString()
    summary.citation = ''
    summary.associated.add(dcHandlers.commonHandlers.loadHierarchyLinkBlocks())

    summary.navBarOverflow = []
    summary.content = handlers.processElements(metadata.children())

    return summary
  }


  def contactsElSxt =  { metadata ->
    def contacts = metadata
            .'dc:creator'
            .collect {el -> el.text().replace(",","")}
            .findAll()  // discard blank/void
            .unique()
            .collect {el -> [name : el, emptyName : false]}
    return contacts ?
            handlers.fileResult("html/sxt-contacts.html", [contacts : contacts]):
            ""
  }

  def keywordsElSxt = { metadata ->
    def thesaurusName = f.translate("noThesaurusName")
    def keywords = metadata
            .'dc:subject'
            .collect {el -> el.text()}
            .findAll()  // discard blank/void
            .unique()
    def replacements = [label   : f.nodeLabel("dc:subject", null), keywords: [(thesaurusName): keywords]]
    return keywords ?
            handlers.fileResult('html/sxt-keyword.html',replacements):
            ""
  }

  def constraintsElSxt = { metadata ->
    def constraints = metadata
            .'dc:rights'
            .collect {el -> el.text()}
            .findAll()  // discard blank/void
            .unique()
    def replacements = [
            otherConstraints: constraints,
            otherConstraintsLabel : f.nodeLabel("dc:rights", null)]
    return constraints ?
            handlers.fileResult("html/sxt-constraints.html", replacements):
            ""
  }

  def datesElSxt =  { metadata ->
    return  metadata
            .'dc:date'
            .collect {el -> el.text()}
            .findAll()  // discard blank/void
            .unique()
            .inject ('', {dates, date -> dates + '<p>' + date + '</p>'})
  }

  def thumbnailsElSxt = { metadata ->
    return metadata
            .'dct:references'
            .collect {el -> el.text()}
            .findAll()  // discard blank/void
            .unique()
  }

  def bboxElSxt = {  metadata ->
    if (metadata."dc:coverage"[0]) {
      def replacements = bbox(metadata."dc:coverage"[0])
      replacements['label'] = f.nodeLabel("dc:coverage", null) + " (" + f.translate('inclusive') + ")"
      replacements['pdfOutput'] = false
      replacements['gnUrl'] = env.getLocalizedUrl();

      return handlers.fileResult("html/bbox.html", replacements)
    }
  }

  def bbox(el) {
    def mapConfig = env.mapConfiguration
    mapConfig.setWidth(mapConfig.thumbnailWidth)

    // splitting something like this : "North 90, South -90, East 180, West -180.  (Global)"
    def coord = el
      .text()
      .split(',')
      .inject([:], {cs, c ->
        def parts = c.split()
        cs.put(parts[0], parts[1])
        return cs
      })

    return [ w: coord['West'],
             e: coord['East'],
             s: coord['South'],
             n: coord['North'],
             geomproj: "EPSG:4326",
             minwidth: mapConfig.getWidth() / 4,
             minheight: mapConfig.getWidth() / 4,
             mapconfig: mapConfig
    ]
  }
}
