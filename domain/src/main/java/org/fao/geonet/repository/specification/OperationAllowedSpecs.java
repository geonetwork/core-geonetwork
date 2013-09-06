package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;
import org.fao.geonet.domain.ReservedOperation;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public final class OperationAllowedSpecs {
    private OperationAllowedSpecs() {
        // disallow instantiation
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct metadataId.
     *
     * @param metadataId the id to match
     */
    public static Specification<OperationAllowed> hasMetadataId(final String metadataId) {
        return hasMetadataId(Integer.valueOf(metadataId));
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct metadataId.
     *
     * @param metadataId the id to match
     */
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

    /**
     * A specification that is limits results to opAllowed objects that have the correct groupId.
     *
     * @param groupId the id to match
     */
    public static Specification<OperationAllowed> hasGroupId(final int groupId) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> mdIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.groupId);
                Predicate mdIdEqualPredicate = cb.equal(mdIdAttributePath, cb.literal(groupId));
                return mdIdEqualPredicate;
            }
        };
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct operationId.
     *
     * @param operation the operation to match
     */
    public static Specification<OperationAllowed> hasOperation(final ReservedOperation operation) {
        return hasOperationId(operation.getId());
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct operationId.
     *
     * @param operationId the id to match
     */
    public static Specification<OperationAllowed> hasOperationId(final int operationId) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> mdIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.operationId);
                Predicate mdIdEqualPredicate = cb.equal(mdIdAttributePath, cb.literal(operationId));
                return mdIdEqualPredicate;
            }
        };
    }
}
