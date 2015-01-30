package org.fao.geonet.repository.specification;


import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeed_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public class InspireAtomFeedSpecs {
    private InspireAtomFeedSpecs() {
        // no instantiation
    }


    public static Specification<InspireAtomFeed> hasMetadataId(final int metadataId) {
        return new Specification<InspireAtomFeed>() {
            @Override
            public Predicate toPredicate(Root<InspireAtomFeed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(InspireAtomFeed_.metadataId);
                Predicate idEqualPredicate = cb.equal(userIdAttributePath, cb.literal(metadataId));
                return idEqualPredicate;
            }
        };
    }

}
