package iso19139sxt

public class Handlers extends iso19139.Handlers {
    public Handlers(handlers, f, env) {
        super(handlers, f, env);
    }

    def keywordsElSxt = {keywords ->
        def keywordProps = com.google.common.collect.ArrayListMultimap.create()
        keywords.collectNested {it.'**'.findAll{it.name() == 'gmd:keyword'}}.flatten().each { k ->
            def thesaurusName = isofunc.isoText(k.parent().'gmd:thesaurusName'.'gmd:CI_Citation'.'gmd:title')

            if (thesaurusName.isEmpty()) {
                def keywordTypeCode = k.parent().'gmd:type'.'gmd:MD_KeywordTypeCode'
                if (!keywordTypeCode.isEmpty()) {
                    thesaurusName = f.translate("uncategorizedKeywords")
                }
            }

            if (thesaurusName.isEmpty()) {
                thesaurusName = f.translate("noThesaurusName")
            }
            keywordProps.put(thesaurusName, isofunc.isoText(k))
        }

        return handlers.fileResult('html/sxt-keyword.html', [
                label : f.nodeLabel("gmd:descriptiveKeywords", null),
                keywords: keywordProps.asMap()])
    }


    def bboxElSxt(thumbnail) {
        return { el ->
            if (el.parent().'gmd:EX_BoundingPolygon'.text().isEmpty() &&
                    el.parent().parent().'gmd:geographicElement'.'gmd:EX_BoundingPolygon'.text().isEmpty()) {
                def replacements = bbox(thumbnail, el)
                replacements['label'] = f.nodeLabel(el)
                //replacements['pdfOutput'] = env.formatType == FormatType.pdf

                handlers.fileResult("html/sxt-bbox.html", replacements)
            }
        }
    }

    def dataQualityInfoElSxt =  { el ->
        return handlers.fileResult('html/sxt-statements.html', [
                statements : el
        ])
    }

    def datesElSxt =  { els ->
        def dates = ''
        els.each { el ->
            def date = el.'gmd:date'.'gco:Date'.text().isEmpty() ?
                    el.'gmd:date'.'gco:DateTime'.text() :
                    el.'gmd:date'.'gco:Date'.text()

            if(date) {
                def dateType = f.codelistValueLabel(el.'gmd:dateType'.'gmd:CI_DateTypeCode')
                dates += '<p>' + date + ' - ' + dateType + '</p>'
            }
        }
        return dates
    }

    def contactsElSxt =  { els ->
        def contacts = []
        def orgs = []
        def idx = 0
        els.each { el ->
            def name = el.'gmd:individualName'
            def org = el.'gmd:organisationName'
            if(name && contacts.indexOf(name) < 0) {
                contacts.push(name)
            }
            if(org && orgs.indexOf(org) < 0) {
                orgs.push(org)
            }
        }
        def replacements = [
                contacts : contacts,
                orgs : orgs
        ]
        return handlers.fileResult("html/sxt-contacts.html", replacements)
    }
}
