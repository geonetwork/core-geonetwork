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

package org.fao.geonet.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * The id object for {@link UserGroup}
 *
 * @author Jesse
 */
@Embeddable
public class UserGroupId implements Serializable {
    private static final long serialVersionUID = 758566280699819800L;

    private int _userId;
    private int _groupId;
    private Profile _profile;

    public UserGroupId() {
        // Default constructor.
    }

    /**
     * Convenenience constructor.
     */
    public UserGroupId(User user, Group group) {
        setUserId(user.getId());
        setGroupId(group.getId());
    }

    /**
     * Get the id of the user.
     *
     * @return the id of the user.
     */
    public int getUserId() {
        return _userId;
    }

    /**
     * Set the id of the user.
     *
     * @param userId the id of the user.
     * @return this id object
     */
    public UserGroupId setUserId(int userId) {
        this._userId = userId;
        return this;
    }

    /**
     * Get the group id.
     *
     * @return the group id.
     */
    public int getGroupId() {
        return _groupId;
    }

    /**
     * Set the group id.
     *
     * @param groupId the group id
     * @return this id object
     */
    public UserGroupId setGroupId(int groupId) {
        this._groupId = groupId;
        return this;
    }

    /**
     * Return the profile for this relation.
     *
     * @return the profile for this relation.
     */
    public Profile getProfile() {
        return _profile;
    }

    /**
     * Set the profile for this relation.
     *
     * @param profile the profile for this relation.
     * @return this entity object
     */
    public UserGroupId setProfile(Profile profile) {
        this._profile = profile;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _groupId;
        result = prime * result + _userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserGroupId other = (UserGroupId) obj;
        if (_groupId != other._groupId)
            return false;
        if (_userId != other._userId)
            return false;
        return true;
    }


}
