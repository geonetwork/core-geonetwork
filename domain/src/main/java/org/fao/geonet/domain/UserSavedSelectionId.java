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
 * The id object for {@link UserSavedSelection}
 *
 */
@Embeddable
public class UserSavedSelectionId implements Serializable {
    private static final long serialVersionUID = 758566280699819800L;

    private int _userId;
    private int _selectionId;
    private String _metadataUuid;

    public UserSavedSelectionId() {
        // Default constructor.
    }

    /**
     * Convenenience constructor.
     */
    public UserSavedSelectionId(Selection selection, User user, String metadataUuid) {
        setUserId(user.getId());
        setSelectionId(selection.getId());
        setMetadataUuid(metadataUuid);
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
    public UserSavedSelectionId setUserId(int userId) {
        this._userId = userId;
        return this;
    }

    /**
     * Get the selection id.
     *
     * @return the selection id.
     */
    public int getSelectionId() {
        return _selectionId;
    }

    /**
     * Set the selection id.
     *
     * @param selectionId the selection id
     * @return this id object
     */
    public UserSavedSelectionId setSelectionId(int selectionId) {
        this._selectionId = selectionId;
        return this;
    }

    /**
     * Return the metadata record uuid
     * for this user and selection.
     *
     * @return the metadata record uuid
     * for this user and selection.
     */
    public String getMetadataUuid() {
        return _metadataUuid;
    }

    /**
     * Set the list of metadata record ids
     * for this user and selection.
     *
     * @param metadataUuid
     * @return this entity object
     */
    public UserSavedSelectionId setMetadataUuid(String metadataUuid) {
        this._metadataUuid = metadataUuid;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
            "Selection '%d' user '%d', uuid: '%s'.",
            _selectionId, _userId, _metadataUuid);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _selectionId;
        result = prime * result + _metadataUuid.hashCode();
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
        UserSavedSelectionId other = (UserSavedSelectionId) obj;
        if (_selectionId != other._selectionId)
            return false;
        if (_userId != other._userId)
            return false;
        return true;
    }
}
