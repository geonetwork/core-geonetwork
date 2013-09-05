package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataSpecs {
    private MetadataSpecs() {
        // no instantiation
    }
    
    public static Specification<Metadata> hasMetadataId(final int metadataId) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(Metadata_.id);
                Predicate idEqualPredicate = cb.equal(userIdAttributePath, cb.literal(metadataId));
                return idEqualPredicate;
            }
        };
    }

    public static Specification<Metadata> hasMetadataUuid(final String uuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(Metadata_.uuid);
                Predicate uuidEqualPredicate = cb.equal(userNameAttributePath, cb.literal(uuid));
                return uuidEqualPredicate;
            }
        };
    }
    public static Specification<Metadata> hasHarvesterUuid(final String harvesterUuid) {
        return new Specification<Metadata>() {
            @Override
            public Predicate toPredicate(Root<Metadata> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(Metadata_.harvestInfo).get(MetadataHarvestInfo_.uuid);
                Predicate uuidEqualPredicate = cb.equal(userNameAttributePath, cb.literal(harvesterUuid));
                return uuidEqualPredicate;
            }
        };
    }

    
}
