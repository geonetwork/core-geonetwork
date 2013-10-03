package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Path;
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
     * @param <T> the type of date
     * @return a mapping from a time
     */
    <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(T dateInterval, ISODate from, ISODate to);

    /**
     * Count the number of requests for the given groupingPath (and return the value of that group along with the count).
     *
     *
     * @param spec the spec for selecting which elements to analyze.
     * @param groupingPath the path used to group the requests
     *
     * @param direction
     * @return a Pair of &lt;group value, count of requests in that group>
     */
    <T> List<Pair<T, Integer>> getHitSummary(Specification<SearchRequest> spec, PathSpec<SearchRequest, T> groupingPath, Sort.Direction direction);

    /**
     * Get the oldest request date.
     */
    ISODate getOldestRequestDate();

    /**
     * Get the most recent request date.
     */
    ISODate getMostRecentRequestDate();
}
