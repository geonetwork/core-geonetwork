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

package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.metamodel.SingularAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Custom methods for loading {@link UserGroup} entities.
 *
 * @author Jesse
 */
public interface UserGroupRepositoryCustom {
    /**
     * Find all the groupIds that match the specification provided.
     *
     * @param spec a UserGroup selector specification
     */
    List<Integer> findGroupIds(Specification<UserGroup> spec);

    /**
     * Find all the userIds that match the specification provided.
     *
     * @param spec a UserGroup selector specification
     */
    List<Integer> findUserIds(Specification<UserGroup> spec);

    /**
     * Delete all the UserGroups with an id in the collection of ids.  The component of the id that
     * is used in the 'in' clause is determined by the idAttribute.
     *
     * @param idAttribute the part of the id object that is compared to the collection of ids
     * @param ids         the ids for finding the {@link UserGroup} to delete.
     * @return the number of entities deleted
     */
    int deleteAllByIdAttribute(SingularAttribute<UserGroupId, Integer> idAttribute, Collection<Integer> ids);

    /**
     * Update user with the new list of {@link UserGroup}. 
     * If the user already has all the groups specified then no change will be made.
     *
     * @param userId        user id to have the groups updated
     * @param newUserGroups the {@link UserGroup} to set
     */
    void updateUserGroups(int userId, Set<UserGroup> newUserGroups);
}
