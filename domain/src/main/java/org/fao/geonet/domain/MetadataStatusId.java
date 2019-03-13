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

import javax.persistence.*;

import java.io.Serializable;

/**
 * The id object of {@link MetadataStatus}.
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataStatusId implements Serializable {
    private static final long serialVersionUID = -4395314364468537427L;
    private ISODate _changedate;
    private int _metadataId;
    private int _statusId;
    private int _userId;

    /**
     * Get the date of the status change in string form.
     *
     * @return the date of the status change in string form.
     */
    @AttributeOverride(name = "dateAndTime", column = @Column(name = "changeDate", nullable = false, length = 30))
    public ISODate getChangeDate() {
        return _changedate;
    }

    /**
     * Set the date of the status change in string form.
     *
     * @param changedate the date of the status change in string form.
     */
    public MetadataStatusId setChangeDate(ISODate changedate) {
        this._changedate = changedate;
        return this;
    }

    /**
     * Get the id of the metadata the status is related to.
     *
     * @return the id of the metadata the status is related to.
     */
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the id of the metadata the status is related to.
     *
     * @param metadataId the id of the metadata the status is related to.
     * @return this id object
     */
    public MetadataStatusId setMetadataId(int metadataId) {
        this._metadataId = metadataId;
        return this;
    }

    /**
     * Get the id of the new status.
     *
     * @return the id of the new status.
     */
    public int getStatusId() {
        return _statusId;
    }

    /**
     * Set the id of the new status.
     *
     * @param statusId the id of the new status.
     * @return this id object
     */
    public MetadataStatusId setStatusId(int statusId) {
        this._statusId = statusId;
        return this;
    }

    /**
     * Get the user who is responsible for changing the status.
     *
     * @return the user who is responsible for changing the status.
     */
    public int getUserId() {
        return _userId;
    }

    /**
     * Set the user who changed the status.
     *
     * @param userId the user who changed the status.
     * @return this id object
     */
    public MetadataStatusId setUserId(int userId) {
        this._userId = userId;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_changedate == null) ? 0 : _changedate.hashCode());
        result = prime * result + _metadataId;
        result = prime * result + _statusId;
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
        MetadataStatusId other = (MetadataStatusId) obj;
        if (_changedate == null) {
            if (other._changedate != null)
                return false;
        } else if (!_changedate.equals(other._changedate))
            return false;
        if (_metadataId != other._metadataId)
            return false;
        if (_statusId != other._statusId)
            return false;
        if (_userId != other._userId)
            return false;
        return true;
    }

	@Override
	public String toString() {
		return "MetadataStatusId [" + (_changedate != null ? "_changedate=" + _changedate + ", " : "") + "_metadataId="
				+ _metadataId + ", _statusId=" + _statusId + ", _userId=" + _userId + "]";
	}
}
