package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId_;
import org.fao.geonet.domain.MetadataRelation_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Specifications for querying {@link org.fao.geonet.repository.UserRepository}.
 *
 * @author Jesse
 */
public final class MetadataRelationSpecs {
    private MetadataRelationSpecs() {
        // no instantiation
    }

    public static Specification<MetadataRelation> hasMetadataId(final Integer metadataId) {
        return new Specification<MetadataRelation>() {
            @Override
            public Predicate toPredicate(Root<MetadataRelation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> metadataIdPath = root.get(MetadataRelation_.id).get(MetadataRelationId_.metadataId);
                return cb.equal(metadataIdPath, metadataId);
            }
        };
    }

    public static Specification<MetadataRelation> hasRelatedId(final Integer relatedId) {
        return new Specification<MetadataRelation>() {
            @Override
            public Predicate toPredicate(Root<MetadataRelation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Path<Integer> relatedIdPath = root.get(MetadataRelation_.id).get(MetadataRelationId_.relatedId);
                return cb.equal(relatedIdPath, relatedId);
            }
        };
    }
}
