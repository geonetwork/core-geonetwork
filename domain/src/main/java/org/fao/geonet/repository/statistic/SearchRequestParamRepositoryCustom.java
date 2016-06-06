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
 * User: Jesse Date: 9/29/13 Time: 7:39 PM
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
