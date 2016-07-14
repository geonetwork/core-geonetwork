/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.metamodel.SingularAttribute;

import java.util.List;

/**
 * Custom query methods for SearchRequest.
 * <p/>
 * User: Jesse Date: 9/30/13 Time: 9:32 PM
 */
public interface SearchRequestRepositoryCustom {
    /**
     * Count all the requests made between the given times.
     *
     * @param dateInterval dateType the grouping and sort type
     * @param from         start time (inclusive)
     * @param to           end time (inclusive)
     * @param <T>          the type of date
     * @return a mapping from a time
     */
    @Nonnull
    <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(
        @Nonnull T dateInterval, @Nonnull ISODate from, @Nonnull ISODate to);

    /**
     * Count all the requests made between the given times restricted by the spec
     *
     * @param dateInterval dateType the grouping and sort type
     * @param from         start time (inclusive)
     * @param to           end time (inclusive)
     * @param spec         a spec restricting the records counted
     * @param <T>          the type of date
     * @return a mapping from a time
     */
    @Nonnull
    <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(
        @Nonnull T dateInterval, @Nonnull ISODate from, @Nonnull ISODate to, @Nonnull Specification<SearchRequest> spec);

    /**
     * Count the number of requests for the given groupingPath (and return the value of that group
     * along with the count).
     *
     * @param spec         the spec for selecting which elements to analyze.
     * @param groupingPath the path used to group the requests
     * @return a Pair of &lt;group value, count of requests in that group>
     */
    <T> List<Pair<T, Integer>> getHitSummary(Specification<SearchRequest> spec, PathSpec<SearchRequest, T> groupingPath,
                                             Sort.Direction direction);

    /**
     * Get the oldest request date.
     */
    ISODate getOldestRequestDate();

    /**
     * Get the most recent request date.
     */
    ISODate getMostRecentRequestDate();

    /**
     * Essentially the query: SELECT DISTINCT [attribute] FROM SearchRequest.
     *
     * @param attribute the attribute to select.
     * @param <T>       the type of the attribute
     * @return All distinct values of the attribute in the SearchRequest table
     */
    <T> List<T> selectAllDistinctAttributes(SingularAttribute<SearchRequest, T> attribute);
}
