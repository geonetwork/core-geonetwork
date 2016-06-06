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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.ISODate_;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Implementation for the custom search methods of {@link org.fao.geonet.repository.statistic.SearchRequestParamRepositoryCustom}.
 * <p/>
 * User: Jesse Date: 9/29/13 Time: 7:42 PM
 */
public class SearchRequestRepositoryImpl implements SearchRequestRepositoryCustom {
    static final String[] TERMS_TO_EXCLUDE_FROM_TAG_CLOUD;

    static {
        final ReservedOperation[] reservedOperations = ReservedOperation.values();
        String[] terms = {"_istemplate", "_locale", "_owner", "_groupowner", "_cat", "_dummy", "type"};
        TERMS_TO_EXCLUDE_FROM_TAG_CLOUD = new String[reservedOperations.length + terms.length];
        System.arraycopy(terms, 0, TERMS_TO_EXCLUDE_FROM_TAG_CLOUD, reservedOperations.length, terms.length);
        for (int i = 0; i < reservedOperations.length; i++) {
            ReservedOperation reservedOperation = reservedOperations[i];
            TERMS_TO_EXCLUDE_FROM_TAG_CLOUD[i] = reservedOperation.getLuceneIndexCode();
        }
    }

    @PersistenceContext
    private EntityManager _EntityManager;

    @Override
    public <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(final T dateInterval, ISODate from,
                                                                                               ISODate to) {
        return internalGetRequestDateToRequestCountBetween(dateInterval, from, to, null);
    }

    @Override
    public <T extends DateInterval>
    List<Pair<T, Integer>> getRequestDateToRequestCountBetween(@Nonnull final T dateInterval,
                                                               @Nonnull final ISODate from,
                                                               @Nonnull final ISODate to,
                                                               @Nonnull final Specification<SearchRequest> spec) {
        return internalGetRequestDateToRequestCountBetween(dateInterval, from, to, spec);
    }

    private <T extends DateInterval> List<Pair<T, Integer>>
    internalGetRequestDateToRequestCountBetween(@Nonnull final T dateInterval, @Nonnull ISODate from, @Nonnull ISODate to,
                                                @Nullable Specification<SearchRequest> spec) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);

        final Root<SearchRequest> requestRoot = cbQuery.from(SearchRequest.class);

        final Path<ISODate> requestDate = requestRoot.get(SearchRequest_.requestDate);
        final Expression<String> requestDateByType = cb.substring(requestDate.get(ISODate_.dateAndTime), 1, dateInterval.getSubstringEnd());

        Predicate whereClause = cb.and(cb.lessThanOrEqualTo(requestDate, to), cb.greaterThanOrEqualTo(requestDate, from));
        if (spec != null) {
            whereClause = cb.and(whereClause, spec.toPredicate(requestRoot, cbQuery, cb));
        }
        final CompoundSelection<Tuple> selection = cb.tuple(requestDateByType, cb.count(requestRoot));
        cbQuery.select(selection)
            .where(whereClause)
            .groupBy(requestDateByType)
            .orderBy(cb.desc(requestDateByType));

        final TypedQuery<Tuple> query = _EntityManager.createQuery(cbQuery);
        List<Tuple> stats = query.getResultList();

        return Lists.transform(stats, new Function<Tuple, Pair<T, Integer>>() {
            @Nullable
            @Override
            public Pair<T, Integer> apply(@Nonnull Tuple stat) {
                final String dateString = stat.get(0, String.class);
                final Long count = stat.get(1, Long.class);

                final T string = (T) dateInterval.createFromString(dateString);
                return Pair.read(string, count.intValue());
            }
        });
    }

    @Override
    public <T> List<Pair<T, Integer>> getHitSummary(Specification<SearchRequest> spec, PathSpec<SearchRequest, T> groupingPath, Sort
        .Direction direction) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SearchRequest> root = query.from(SearchRequest.class);

        final Predicate predicate = spec.toPredicate(root, query, cb);
        Path<T> path = groupingPath.getPath(root);

        final Order order;
        switch (direction) {
            case DESC:
                order = cb.desc(path);
                break;
            case ASC:
                order = cb.asc(path);
                break;
            default:
                throw new IllegalArgumentException("Not a valid value: " + direction);
        }

        query.select(cb.tuple(path, cb.count(root)))
            .where(predicate)
            .groupBy(path)
            .orderBy(order);

        return Lists.transform(_EntityManager.createQuery(query).getResultList(), new Function<Tuple, Pair<T, Integer>>() {
            @Nullable
            @Override
            public Pair<T, Integer> apply(@Nonnull Tuple input) {
                T value = (T) input.get(0);
                Integer count = input.get(1, Long.class).intValue();
                return Pair.read(value, count);
            }
        });
    }

    @Override
    public ISODate getOldestRequestDate() {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<ISODate> query = cb.createQuery(ISODate.class);
        final Root<SearchRequest> requestRoot = query.from(SearchRequest.class);
        query.select(cb.least(requestRoot.get(SearchRequest_.requestDate)));

        return _EntityManager.createQuery(query).getSingleResult();
    }

    @Override
    public ISODate getMostRecentRequestDate() {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<ISODate> query = cb.createQuery(ISODate.class);
        final Root<SearchRequest> requestRoot = query.from(SearchRequest.class);

        query.select(cb.greatest(requestRoot.get(SearchRequest_.requestDate)));

        return _EntityManager.createQuery(query).getSingleResult();
    }

    @Override
    public <T> List<T> selectAllDistinctAttributes(final SingularAttribute<SearchRequest, T> attribute) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<Object> query = cb.createQuery();
        final Root<SearchRequest> requestRoot = query.from(SearchRequest.class);
        query.select(requestRoot.get(attribute)).distinct(true);

        return (List<T>) _EntityManager.createQuery(query).getResultList();
    }

}
