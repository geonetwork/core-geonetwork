package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Element;

public class KeywordLabelSearchClause implements SearchClause {
    KeywordSearchType searchType;
    String keyword;
    boolean ignoreCase;

    public KeywordLabelSearchClause(KeywordSearchType searchType, String keyword, boolean ignoreCase) {
        this.searchType = searchType;
        this.keyword = keyword;
        this.ignoreCase = ignoreCase;
    }

    public void addXmlParams(Element params) {
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pKeyword, keyword);
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pTypeSearch, ""+searchType.ordinal());            
    }

    @Override
    public Where toWhere(Set<String> langs) {
        Where where = Wheres.NONE;
        for (String lang : langs) {
            where = where.or(searchType.toWhere(lang+Selectors.LABEL_POSTFIX,this));
        }
        return where;
    }
}