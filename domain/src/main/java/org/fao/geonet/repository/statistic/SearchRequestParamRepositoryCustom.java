package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Custom query methods for querying the SearchRequestParam enities.
 * <p/>
 * User: Jesse
 * Date: 9/29/13
 * Time: 7:39 PM
 */
public interface SearchRequestParamRepositoryCustom {
    /**
     * Calculate the number of requests per search term.
     *
     * @param limit the max number of elements to show
     * @return a list of search term to number of requests containing that search term.
     */
    @Nonnull
    List<Pair<String, Integer>> getTermTextToRequestCount(@Nonnegative int limit);

    /**
     * Calculate the number of requests per search term.
     *
     * @param limit         the max number of elements to show
     * @param specification a specification to limit what values are returned.
     * @return a list of search term to number of requests containing that search term.
     */
    @Nonnull
    List<Pair<String, Integer>> getTermTextToRequestCount(@Nonnegative int limit, @Nullable Specification<SearchRequestParam> specification);
}
