package org.fao.geonet.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.fao.geonet.domain.User_;
import org.springframework.data.jpa.domain.Specification;

public final class GroupSpecs {

    private GroupSpecs() {
        // don't permit instantiation
    }

    public static Specification<Group> isReserved() {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root,
                    CriteriaQuery<?> query, CriteriaBuilder cb) {
                return getIsReserved(root, cb);
            }
        };
    }

    public static Specification<Group> in(final List<Integer> ids) {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root,
                    CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(root.get(Group_.id).in(ids),
                        cb.not(getIsReserved(root, cb)));
            }
        };
    }

    private static Predicate getIsReserved(Root<Group> root,
            CriteriaBuilder cb) {
        int maxId = Integer.MIN_VALUE;
        for (ReservedGroup reservedGroup : ReservedGroup.values()) {
            if (maxId < reservedGroup.getId()) {
                maxId = reservedGroup.getId();
            }
        }

        return cb.lessThanOrEqualTo(root.get(Group_.id), maxId);
    }

    public static Specification<UserGroup> isEditorOrMore(
            final Integer userId) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root,
                    CriteriaQuery<?> query, CriteriaBuilder cb) {
                root.join(UserGroup_.group);

                Predicate pred = cb
                        .equal(root.get(UserGroup_.user).get(User_.id), userId);

                pred = cb
                        .and(pred,
                                cb.lessThanOrEqualTo(
                                        root.get(UserGroup_.id)
                                                .get(UserGroupId_.profile),
                                        Profile.Editor));

                return pred;
            }
        };
    }
}