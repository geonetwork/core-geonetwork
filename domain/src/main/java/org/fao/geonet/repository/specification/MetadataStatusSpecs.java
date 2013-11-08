package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataStatusSpecs {
    private MetadataStatusSpecs() {
        // no instantiation
    }

    public static Specification<MetadataStatus> hasMetadataId(final int metadataId) {
        return new Specification<MetadataStatus>() {
            @Override
            public Predicate toPredicate(Root<MetadataStatus> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(MetadataStatus_.id).get(MetadataStatusId_.metadataId);
                Predicate idEqualPredicate = cb.equal(userIdAttributePath, cb.literal(metadataId));
                return idEqualPredicate;
            }
        };
    }

    public static Specification<MetadataStatus> hasUserId(final int userId) {
        return new Specification<MetadataStatus>() {
            @Override
            public Predicate toPredicate(Root<MetadataStatus> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(MetadataStatus_.id).get(MetadataStatusId_.userId);
                Predicate uuidEqualPredicate = cb.equal(userIdAttributePath, cb.literal(userId));
                return uuidEqualPredicate;
            }
        };
    }
}
