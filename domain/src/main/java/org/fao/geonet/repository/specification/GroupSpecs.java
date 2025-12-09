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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

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

    public static Specification<Group> inGroupNames(final List<String> groupNames) {
        return new Specification<Group>() {
            @Override
            public Predicate toPredicate(Root<Group> root,
                                         CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.and(root.get(Group_.name).in(groupNames),
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
