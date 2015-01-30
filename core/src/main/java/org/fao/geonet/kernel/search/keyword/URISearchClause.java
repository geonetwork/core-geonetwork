package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Element;

/**
 * A search clause for selecting based on a provided keyowrd URI
 * 
 * @author jeichar
 */
public class URISearchClause implements SearchClause {

    KeywordSearchType searchType;
    private String uri;
    boolean ignoreCase;

    public URISearchClause(String uri) {
        this.uri = uri;
    }

    public URISearchClause(KeywordSearchType searchType, String keywordURI, boolean ignoreCase) {
        this.searchType = searchType;
        this.uri = keywordURI;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public Where toWhere(Set<String> langs) {
        if (this.searchType != null) {
            return searchType.toWhere("id", this.uri, this.ignoreCase);
        } else {
            return Wheres.ID(this.uri);
        }
    }

    @Override
    public void addXmlParams(Element params) {
        params.addContent(new Element(XmlParams.pUri).setText(this.uri));
        KeywordSearchParamsBuilder.addXmlParam(params, XmlParams.pUri, this.uri);
        if (this.searchType != null) {
            KeywordSearchParamsBuilder.addXmlParam(params,
                    XmlParams.pTypeSearch, "" + searchType.ordinal());
        }
    }

}
