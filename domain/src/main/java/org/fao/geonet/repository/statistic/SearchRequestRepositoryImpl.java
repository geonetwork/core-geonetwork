package org.fao.geonet.repository.statistic;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.statistic.SearchRequest;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * Implementation for the custom search methods of {@link org.fao.geonet.repository.statistic.SearchRequestParamRepositoryCustom}.
 * <p/>
 * User: Jesse
 * Date: 9/29/13
 * Time: 7:42 PM
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
    EntityManager _EntityManager;

    @Override
    public <T extends DateInterval> List<Pair<T, Integer>> getRequestDateToRequestCountBetween(final T dateInterval, ISODate from,
                                                                                               ISODate to) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);

        final Root<SearchRequest> paramRoot = cbQuery.from(SearchRequest.class);

        final Path<ISODate> requestDate = paramRoot.get(SearchRequest_.requestDate);
        final Expression<String> requestDateByType = cb.substring(requestDate.as(String.class), 1, dateInterval.getSubstringEnd());

        final CompoundSelection<Tuple> selection = cb.tuple(requestDateByType, cb.count(paramRoot));
        cbQuery.select(selection)
                .where(cb.and(cb.lessThanOrEqualTo(requestDate, to), cb.greaterThanOrEqualTo(requestDate, from)))
                .groupBy(requestDateByType)
                .orderBy(cb.desc(requestDateByType));

        List<Tuple> stats = _EntityManager.createQuery(cbQuery).getResultList();

        return Lists.transform(stats, new Function<Tuple, Pair<T, Integer>>() {
            @Nullable
            @Override
            public Pair<T, Integer> apply(@Nullable Tuple stat) {
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
                throw new IllegalArgumentException("Not a valid value: "+direction);
        }

        query.select(cb.tuple(path, cb.count(root)))
                .where(predicate)
                .groupBy(path)
                .orderBy(order);

        return Lists.transform(_EntityManager.createQuery(query).getResultList(), new Function<Tuple, Pair<T, Integer>>() {
            @Nullable
            @Override
            public Pair<T, Integer> apply(@Nullable Tuple input) {
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

}
