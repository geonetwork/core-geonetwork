package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.Collection;

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

    public static Specification<User> hasProfile(final Profile profile) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Profile> profilePath = root.get(User_.profile);
                Predicate userIdEqualPredicate = cb.equal(profilePath, cb.literal(profile));
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

    public static Specification<User> hasAuthType(final String authType) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<String> authTypeAttributePath = root.get(User_.security).get(UserSecurity_.authType);
                Predicate userIdEqualPredicate = cb.equal(authTypeAttributePath, authType);
                return userIdEqualPredicate;
            }
        };
    }

    public static Specification<User> userIsNameNotOneOf(final Collection<String> usernames) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(User_.username).in(usernames);
            }
        };
    }

    public static Specification<User> hasUserIdIn(final Collection<Integer> ids) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(User_.id).in(ids);
            }
        };
    }

    public static Specification<User> hasEnabled(final Boolean enabled) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Boolean> enabledAttributePath = root.get(User_.enabled);
                Predicate enabledEqualPredicate = cb.equal(enabledAttributePath, enabled);
                return enabledEqualPredicate;
            }
        };
    }

    public static Specification<User> loginDateBetweenAndByGroups(final ISODate loginDateFrom,
                                                                  final ISODate loginDateTo,
                                                                  final Collection<Integer> groups) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);

                Path<String> lastLoginDateAttributePath = root.get(User_.lastLoginDate);
                Path<Integer> userIdPath = root.get(User_.id);
                Predicate userLastLoginBetweenPredicate = cb.between(lastLoginDateAttributePath,
                        loginDateFrom.toString(), loginDateTo.toString());

                if (!groups.isEmpty()) {
                    final Root<UserGroup> userGroupRoot = query.from(UserGroup.class);
                    final Path<Integer> groupGPath = userGroupRoot.get(UserGroup_.group).get(Group_.id);
                    final Path<Integer> userGPath = userGroupRoot.get(UserGroup_.user).get(User_.id);

                    Predicate inGroups = groupGPath.in(groups);

                    userLastLoginBetweenPredicate = cb.and(cb.equal(userGPath,
                            userIdPath), cb.and(userLastLoginBetweenPredicate, inGroups));
                }


                return userLastLoginBetweenPredicate;
            }
        };
    }
}
