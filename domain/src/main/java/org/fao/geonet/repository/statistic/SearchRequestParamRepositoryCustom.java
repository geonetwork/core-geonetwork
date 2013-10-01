package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;

import java.util.List;

/**
 * Custom query methods for querying the SearchRequestParam enities.
 *
 * User: Jesse
 * Date: 9/29/13
 * Time: 7:39 PM
 */
public interface SearchRequestParamRepositoryCustom {
    /**
     * Calculate the number of requests per search term.
     *
     * @return a list of search term to number of requests containing that search term.
     * @param limit the max number of elements to show
     */
    List<Pair<String, Integer>> getTagCloudSummary(int limit);
}
