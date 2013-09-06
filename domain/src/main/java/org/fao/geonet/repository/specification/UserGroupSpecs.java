package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public final class UserGroupSpecs {

    private UserGroupSpecs() {
        // don't permit instantiation
    }

    public static Specification<UserGroup> hasGroupId(final int groupId) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> grpIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.groupId);
                Predicate grpIdEqualPredicate = cb.equal(grpIdAttributePath, cb.literal(groupId));
                return grpIdEqualPredicate;
            }
        };
    }

    public static Specification<UserGroup> hasUserId(final int userId) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.userId);
                Predicate userIdEqualPredicate = cb.equal(userIdAttributePath, cb.literal(userId));
                return userIdEqualPredicate;
            }
        };
    }

    public static Specification<UserGroup> hasProfile(final Profile profile) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Profile> userIdAttributePath = root.get(UserGroup_.profile);
                Predicate userIdEqualPredicate = cb.equal(userIdAttributePath, cb.literal(profile));
                return userIdEqualPredicate;
            }
        };

    }
}
