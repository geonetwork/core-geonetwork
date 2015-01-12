package org.fao.geonet.kernel.search;

/**
 * The types of searchers the {@link org.fao.geonet.kernel.search.SearchManager} can create in the
 * {@link org.fao.geonet.kernel.search.SearchManager#newSearcher(SearcherType, String)} method.
 *
* @author Jesse on 12/24/2014.
*/
public enum SearcherType {
    LUCENE, Z3950, UNUSED
}
