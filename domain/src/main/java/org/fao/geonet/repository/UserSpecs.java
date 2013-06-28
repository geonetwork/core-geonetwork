package org.fao.geonet.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSecurity_;
import org.fao.geonet.domain.User_;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for querying {@link UserRepository}.
 * 
 * @author Jesse
 */
public final class UserSpecs {
    private UserSpecs() {
        // no instantiation
    }
    
    public static Specification<User> hasUserId(final int userId) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> userIdAttributePath = root.get(User_.id);
                Predicate userIdEqualPredicate = cb.equal(userIdAttributePath, cb.literal(userId));
                return userIdEqualPredicate;
            }
        };
    }

    public static Specification<User> hasUserName(final String userName) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> userNameAttributePath = root.get(User_.username);
                Predicate userIdEqualPredicate = cb.equal(userNameAttributePath, cb.literal(userName));
                return userIdEqualPredicate;
            }
        };
    }
    
    
    public static Specification<User> hasNullAuthType() {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> authTypeAttributePath = root.get(User_.security).get(UserSecurity_.authType);
                Predicate userIdEqualPredicate = cb.isNull(authTypeAttributePath);
                return userIdEqualPredicate;
            }
        };
    }
    
}
