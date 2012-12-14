package org.fao.geonet.kernel.search;

import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.search.Filter;

/**
 * Class to make available the chained filters. Used in LanguageSearcher
 */
public class GeoNetworkChainedFilter extends ChainedFilter {
	private Filter[] filters;

    public Filter[] getFilters() {
          return filters;
    }

    public GeoNetworkChainedFilter(Filter[] filters) {
        super(filters);
        this.filters = filters;
    }

    public GeoNetworkChainedFilter(Filter[] filters, int[] ints) {
        super(filters, ints);
        this.filters = filters;
    }

    public GeoNetworkChainedFilter(Filter[] filters, int i) {
        super(filters, i);
        this.filters = filters;
    }


}
