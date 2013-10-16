package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.domain.statistic.SearchRequestParam_;
import org.fao.geonet.domain.statistic.SearchRequest_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;

/**
 * Specifications for making queries on {@link org.fao.geonet.domain.statistic.SearchRequest} Entities.
 * <p/>
 * User: Jesse
 * Date: 10/2/13
 * Time: 7:37 AM
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
