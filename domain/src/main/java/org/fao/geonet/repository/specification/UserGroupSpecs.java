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

import org.fao.geonet.domain.Group_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId_;
import org.fao.geonet.domain.UserGroup_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UserGroupSpecs {

    private UserGroupSpecs() {
        // don't permit instantiation
    }

    public static Specification<UserGroup> hasGroupId(final Integer groupId) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> grpIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.groupId);
                Predicate grpIdEqualPredicate;
                if (groupId == null) {
                    grpIdEqualPredicate = cb.isNull(grpIdAttributePath);
                } else {
                    grpIdEqualPredicate = cb.equal(grpIdAttributePath, cb.literal(groupId));
                }
                return grpIdEqualPredicate;
            }
        };
    }

    public static Specification<UserGroup> hasGroupIds(final List<Integer> groupId) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> grpIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.groupId);
                Predicate grpIdInPredicate = grpIdAttributePath.in(groupId);
                return grpIdInPredicate;
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

    /**
     * Specification for retrieving all the userGroups with a given profile.
     *
     * @param profile The {@link Profile} to filter the userGroups.
     * @return the query.
     */
    public static Specification<UserGroup> hasProfile(final Profile profile) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Profile> profileIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.profile);
                Predicate profileIdEqualPredicate = cb.equal(profileIdAttributePath, cb.literal(profile));
                return profileIdEqualPredicate;
            }
        };
    }

    /**
     * Specification for retrieving all the userGroups with a {@link Profile} in a given set of profiles.
     *
     * @param profiles The {@link Set} of {@link Profile} to filter the userGroups.
     * @return the query.
     */
    public static Specification<UserGroup> hasProfileIn(final Set<Profile> profiles) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Profile> profileIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.profile);
                Predicate profileIdInPredicate = profileIdAttributePath.in(profiles);
                return profileIdInPredicate;
            }
        };
    }

    /**
     * Specification for retrieving all the userGroups of a given user with a given profile.
     *
     * @param userId  the user id.
     * @param profile the user profile. The exact profile, it doesn't run a hierarchical search.
     * @return the query.
     */
    public static Specification<UserGroup> hasUserIdAndProfile(final int userId, final Profile profile) {
        return (root, query, cb) -> {
            Path<Profile> profileIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.profile);
            Path<Integer> userIdAttributePath = root.get(UserGroup_.id).get(UserGroupId_.userId);
            Predicate profileIdEqualPredicate = cb.equal(profileIdAttributePath, cb.literal(profile));
            Predicate userIdEqualsPredicate = cb.equal(userIdAttributePath, cb.literal(userId));
            return cb.and(profileIdEqualPredicate, userIdEqualsPredicate);
        };
    }


    /**
     * Specification for testing if the UserGroup is (or is not) a reserved group.
     *
     * @param isReservedGroup true if the groups should be a reserved group.
     * @return Specification for testing if the UserGroup is (or is not) a reserved group.
     */
    public static Specification<UserGroup> isReservedGroup(final boolean isReservedGroup) {
        return new Specification<UserGroup>() {
            @Override
            public Predicate toPredicate(Root<UserGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                HashSet<Integer> ids = new HashSet<Integer>();
                for (ReservedGroup reservedGroup : ReservedGroup.values()) {
                    ids.add(reservedGroup.getId());
                }

                Predicate inIdsPredicate = root.get(UserGroup_.group).get(Group_.id).in(ids);

                if (isReservedGroup) {
                    return inIdsPredicate;
                } else {
                    return inIdsPredicate.not();
                }
            }
        };
    }
}
