/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

import java.util.Collection;
import java.util.Set;

/**
 * Specification for querying {@link UserRepository}.
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

    public static Specification<User> hasEmail(final String email) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.isMember(email, root.get(User_.emailAddresses));
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
