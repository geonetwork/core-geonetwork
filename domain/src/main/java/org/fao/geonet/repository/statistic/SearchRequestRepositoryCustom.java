package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;

import java.util.List;

/**
 * Custom query methods for SearchRequest.
 *
 * User: Jesse
 * Date: 9/30/13
 * Time: 9:32 PM
 */
public interface SearchRequestRepositoryCustom {

    /**
     * Count all the requests made between the given times.
     *
     * @param dateInterval dateType the grouping and sort type
     * @param from start time (inclusive)
     * @param to end time (inclusive)
     *
     * @return a mapping from a time
     */
    <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(T dateInterval, ISODate from, ISODate to);
}
