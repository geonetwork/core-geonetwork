package org.fao.geonet.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;
import org.springframework.data.jpa.domain.Specification;

public final class OperationAllowedSpecs {
    private OperationAllowedSpecs() {
        // disallow instantiation
    }
    public static Specification<OperationAllowed> hasMetadataId(final int metadataId) {
        return new Specification<OperationAllowed>() {
            
            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> mdIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId);
                Predicate mdIdEqualPredicate = cb.equal(mdIdAttributePath, cb.literal(metadataId));
                return mdIdEqualPredicate;
            }
        };
    }
}
