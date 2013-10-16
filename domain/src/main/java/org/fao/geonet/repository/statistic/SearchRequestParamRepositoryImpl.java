package org.fao.geonet.repository.statistic;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.domain.statistic.SearchRequestParam_;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * Implementation for the custom search methods of {@link SearchRequestParamRepositoryCustom}.
 * <p/>
 * User: Jesse
 * Date: 9/29/13
 * Time: 7:42 PM
 */
public class SearchRequestParamRepositoryImpl implements SearchRequestParamRepositoryCustom {
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
    public List<Pair<String, Integer>> getTermTextToRequestCount(int limit) {
        return getTermTextToRequestCount(limit, null);
    }

    @Override
    public List<Pair<String, Integer>> getTermTextToRequestCount(int limit, Specification<SearchRequestParam> spec) {
        final CriteriaBuilder cb = _EntityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createQuery(Tuple.class);

        final Root<SearchRequestParam> paramRoot = cbQuery.from(SearchRequestParam.class);

        final Path<String> termFieldPath = paramRoot.get(SearchRequestParam_.termField);
        final Path<String> termTextPath = paramRoot.get(SearchRequestParam_.termText);
        final Predicate notInExcludedTerms = cb.not(cb.trim(termFieldPath).in(TERMS_TO_EXCLUDE_FROM_TAG_CLOUD));
        final Expression<Long> countExpr = cb.count(paramRoot);
        final Predicate notEmptyTermText = cb.notEqual(cb.trim(termTextPath), "");

        final Predicate baseWherePredicate = cb.and(notInExcludedTerms, notEmptyTermText);
        Predicate finalPredicate = baseWherePredicate;

        if (spec != null) {
            finalPredicate = cb.and(finalPredicate, spec.toPredicate(paramRoot, cbQuery, cb));
        }

        cbQuery.select(cb.tuple(termTextPath, countExpr))
                .where(finalPredicate)
                .groupBy(termTextPath)
                .orderBy(cb.desc(countExpr));

        final TypedQuery<Tuple> query = _EntityManager.createQuery(cbQuery);
        query.setMaxResults(limit);
        return Lists.transform(query.getResultList(), new Function<Tuple, Pair<String, Integer>>() {
            @Nullable
            @Override
            public Pair<String, Integer> apply(@Nonnull Tuple input) {
                return Pair.read(input.get(0, String.class), input.get(1, Long.class).intValue());
            }
        });
    }
}
