package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Where;
import org.jdom.Element;

/**
 * Represents a criteria for searching for Keywords
 * 
 * @author jeichar
 */
public interface SearchClause {
    /**
     * Create a Where that selects for this clause
     *
     * @param langs the languages to select for if applicable
     * @return a Where that selects for this clause
     */
    public Where toWhere(Set<String> langs);

    /**
     * Add a representation of this clause to the search params
     * 
     * @param params
     */
    public void addXmlParams(Element params);
    
}
