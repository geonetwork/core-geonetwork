package org.fao.geonet.kernel.search;

import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;

/**
 * Class to make avalaible the cached filter. Used in LanguageSearcher
 *
 * JE: Want to delete along with LanguageSearcher
 */
public class GeoNetworkCachingWrapperFilter extends CachingWrapperFilter {
    private Filter _filter;

    public Filter getFilter() {
        return _filter;
    }

    public GeoNetworkCachingWrapperFilter(Filter filter) {
        super(filter);
        this._filter = filter;
    }
}
