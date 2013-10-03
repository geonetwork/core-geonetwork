package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public final class GroupSpecs {

    private GroupSpecs() {
        // don't permit instantiation
    }

    public static Specification<Group> isReserved() {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                int maxId = Integer.MIN_VALUE;
                for (ReservedGroup reservedGroup : ReservedGroup.values()) {
                    if (maxId < reservedGroup.getId()) {
                        maxId = reservedGroup.getId();
                    }
                }

                return cb.lessThanOrEqualTo(root.get(Group_.id), maxId);
            }
        };
    }
}
