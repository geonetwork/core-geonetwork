package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.ReservedGroup;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
