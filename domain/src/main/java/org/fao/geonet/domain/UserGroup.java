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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.fao.geonet.entitylistener.UserGroupEntityListenerManager;
import org.jdom.Element;

import javax.persistence.*;

import java.io.Serializable;
import java.util.IdentityHashMap;

/**
 * The mapping between user, the groups a user is a part of and the profiles the user has for each
 * group.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = UserGroupNamedQueries.TABLE_NAME)
@EntityListeners(UserGroupEntityListenerManager.class)
public class UserGroup extends GeonetEntity implements Serializable {
    private UserGroupId _id = new UserGroupId();
    private Group _group;
    private User _user;

    /**
     * Get the id object.
     *
     * @return the id object.
     */
    @EmbeddedId
    public UserGroupId getId() {
        return _id;
    }

    /**
     * Set the id object.
     *
     * @param id the id object.
     * @return this usergroup entity
     */
    public UserGroup setId(UserGroupId id) {
        this._id = id;
        return this;
    }

    /**
     * Get the group in the relation. The group is lazily loaded.
     *
     * @return the group in the relation.
     */
    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId", referencedColumnName = "id")
    public Group getGroup() {
        return _group;
    }

    /**
     * Set the group in the relation.
     *
     * @param group the group in the relation.
     * @return this entity object
     */
    public UserGroup setGroup(Group group) {
        this._group = group;
        getId().setGroupId(group.getId());
        return this;
    }

    /**
     * Return the user in the relation. The user is lazily loaded.
     *
     * @return the user in the relation.
     */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User getUser() {
        return _user;
    }

    /**
     * Set the user in the relation.
     *
     * @param user the user in the relation.
     * @return this enity object
     */
    public UserGroup setUser(User user) {
        this._user = user;
        getId().setUserId(_user.getId());
        return this;
    }

    /**
     * Return the profile for this relation.
     *
     * @return the profile for this relation.
     */
    @Transient
    public Profile getProfile() {
        return getId().getProfile();
    }

    /**
     * Set the profile for this relation.
     *
     * @param profile the profile for this relation.
     * @return this entity object
     */
    public UserGroup setProfile(Profile profile) {
        getId().setProfile(profile);
        return this;
    }

    @Override
    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        return new Element("record")
            .addContent(new Element("group").setText("" + getId().getGroupId()))
            .addContent(new Element("user").setText("" + getId().getUserId()))
            .addContent(new Element("profile").setText(getProfile().name()));
    }
}
