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
        el
    }

}
