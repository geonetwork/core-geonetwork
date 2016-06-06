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

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.domain.statistic.SearchRequestParam_;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

import java.util.Collection;

/**
 * Specifications for making queries on {@link org.fao.geonet.domain.statistic.SearchRequest}
 * Entities.
 * <p/>
 * User: Jesse Date: 10/2/13 Time: 7:37 AM
 */
public final class SearchRequestParamSpecs {
    private SearchRequestParamSpecs() {
        // utility classes should not have visible constructors
    }

    /**
     * Create a specification for querying by termField.
     *
     * @param termField the number of termField.
     * @return a specification for querying termField.
     */
    public static Specification<SearchRequestParam> hasTermField(final String termField) {
        return new Specification<SearchRequestParam>() {
            @Override
            public Predicate toPredicate(Root<SearchRequestParam> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> termFieldPath = root.get(SearchRequestParam_.termField);
                final Predicate termFieldPredicate = cb.equal(termFieldPath, termField);
                return termFieldPredicate;
            }
        };
    }

    /**
     * Create a specification for querying by serviceId.
     *
     * @param serviceId the number of serviceId.
     * @return a specification for querying by serviceId.
     */
    public static Specification<SearchRequestParam> hasService(final String serviceId) {
        return new Specification<SearchRequestParam>() {
            @Override
            public Predicate toPredicate(Root<SearchRequestParam> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> termFieldPath = root.get(SearchRequestParam_.request).get(SearchRequest_.service);
                final Predicate serviceEquals = cb.equal(termFieldPath, serviceId);
                return serviceEquals;
            }
        };
    }

    public static Specification<SearchRequestParam> hasTermFieldIn(final Collection<String> termFields) {
        return new Specification<SearchRequestParam>() {
            @Override
            public Predicate toPredicate(Root<SearchRequestParam> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<String> termFieldPath = root.get(SearchRequestParam_.termField);
                return termFieldPath.in(termFields);
            }
        };
    }
}
