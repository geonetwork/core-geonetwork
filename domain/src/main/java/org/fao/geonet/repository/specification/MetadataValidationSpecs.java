package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.MetadataValidation_;
import org.fao.geonet.domain.MetadataValidationId_;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Specification for querying {@link org.fao.geonet.repository.MetadataValidationRepository}.
 *
 * @author Jose García
 */
public class MetadataValidationSpecs {
    private MetadataValidationSpecs() {
        // no instantiation
    }

    public static Specification<MetadataValidation> isInvalidAndRequiredForMetadata(final int metadataId) {
        return new Specification<MetadataValidation>() {
            @Override
            public Predicate toPredicate(Root<MetadataValidation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<MetadataValidationStatus> statusAttributePath = root.get(MetadataValidation_.status);
                Path<Boolean> requiredAttributePath = root.get(MetadataValidation_.required);
                Path<Integer> metadataIdAttributePath = root.get(MetadataValidation_.id).get(MetadataValidationId_.metadataId);

                return cb.and(cb.equal(metadataIdAttributePath, cb.literal(metadataId)),
                        cb.and(cb.equal(statusAttributePath, cb.literal(MetadataValidationStatus.INVALID)),
                                cb.equal(requiredAttributePath, cb.literal(Boolean.TRUE))));
            }
        };
    }

    public static Specification<MetadataValidation> hasMetadataId(final int metadataId) {
        return new Specification<MetadataValidation>() {
            @Override
            public Predicate toPredicate(Root<MetadataValidation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> metadataIdAttributePath = root.get(MetadataValidation_.id).get(MetadataValidationId_.metadataId);

                return cb.equal(metadataIdAttributePath, cb.literal(metadataId));
            }
        };
    }
}
