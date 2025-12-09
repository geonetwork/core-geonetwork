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

import org.fao.geonet.entitylistener.UserMetadataSelectionEntityListenerManager;
import org.springframework.web.bind.annotation.Mapping;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * The mapping between user, a selection and a set of records.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(
    name = "UserSavedSelections",
    indexes = {@Index(name = "idx_usersavedselections_metadatauuid", columnList = "metadataUuid")}
)
@EntityListeners(UserMetadataSelectionEntityListenerManager.class)
public class UserSavedSelection extends GeonetEntity implements Serializable {
    private UserSavedSelectionId _id = new UserSavedSelectionId();

    private Selection _selection;
    private User _user;
    private String _metadataUuid;

    public UserSavedSelection() {
    }

    public UserSavedSelection(Selection s, User u, String _metadataUuid){
        setId(s, u, _metadataUuid);
    }

    /**
     * Get the id object.
     *
     * @return the id object.
     */
    @EmbeddedId
    public UserSavedSelectionId getId() {
        return _id;
    }

    /**
     * Set the id object.
     *
     * @param id the id object.
     * @return this userMetadataSelection entity
     */
    public UserSavedSelection setId(UserSavedSelectionId id) {
        this._id = id;
        return this;
    }
    public UserSavedSelection setId(Selection s, User u, String uuid) {
        _selection = s;
        _user = u;
        _metadataUuid = uuid;
        setId(new UserSavedSelectionId(s, u, uuid));
        return this;
    }


    @MapsId("_userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
        getId().setUserId(_user.getId());
    }

    @MapsId("_selectionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selectionId", referencedColumnName = "id")
    public Selection getSelection() {
        return _selection;
    }

    public void setSelection(Selection selection) {
        _selection = selection;
        getId().setSelectionId(_selection.getId());
    }


    /**
     * Get the metadata uuid of the metadata that the notification is for.
     *
     * @return the metadata uuid of the metadata that the notification is for.
     */
    @MapsId("_metadataUuid")
    @Column(name = "metadataUuid", insertable = false, updatable = false)
    @JoinColumn(table = "metadata",
        referencedColumnName = "uuid", insertable = false, updatable = false)
    public String getMetadataUuid() {
        return _metadataUuid;
    }

    /**
     * Set the metadata uuid of the metadata.
     *
     * @param metadataUuid the metadata uuid of the metadata.
     */
    public void setMetadataUuid(String metadataUuid) {
        this._metadataUuid = metadataUuid;
    }


    @Override
    public String toString() {
        return "UserPersistentSelection: [" + _id.toString() + "]";
    }
}
