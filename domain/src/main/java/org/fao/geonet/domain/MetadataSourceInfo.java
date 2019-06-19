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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Information about the source and owner of the metadata document. This is a JPA Embeddable object
 * that is embedded into a {@link Metadata} Entity
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataSourceInfo {
    private String _sourceId;
    private Integer _groupOwner;
    private Integer _owner;

    /**
     * Get the source of the metadata. The source identifies where the metadata came from. It is
     * usually a uuid but can be any identifier. Normally if the metadata is harvested this will be
     * harvester uuid and if it is locally created it will typically be the siteid of the geonetwork
     * instance.
     *
     * @return the source of the metadata.
     */
    @Column(name = "source", nullable = false)
    public String getSourceId() {
        return _sourceId;
    }

    /**
     * Set the source of the metadata. The source identifies where the metadata came from. It is
     * usually a uuid but can be any identifier. Normally if the metadata is harvested this will be
     * harvester uuid and if it is locally created it will typically be the siteid of the geonetwork
     * instance.
     *
     * @param sourceId the source of the metadata.
     */
    public MetadataSourceInfo setSourceId(String sourceId) {
        this._sourceId = sourceId;
        return this;
    }

    /**
     * Get the group id that owns this metadata. A user can be part of several groups thus the group
     * owner determines which one of those groups the metadata belongs to.
     *
     * @return the group that owns this metadata.
     */
    @Column(name = "groupOwner")
    public Integer getGroupOwner() {
        return _groupOwner;
    }

    /**
     * Set the group id that owns this metadata. A user can be part of several groups thus the group
     * owner determines which one of those groups the metadata belongs to.
     *
     * @param groupOwner the group id that owns this metadata.
     */
    public MetadataSourceInfo setGroupOwner(Integer groupOwner) {
        this._groupOwner = groupOwner;
        return this;
    }

    /**
     * Get the id of the user that owns this metadata.
     *
     * @return the id of the user that owns this metadata.
     */
    @Column(nullable = false)
    public Integer getOwner() {
        return _owner;
    }

    /**
     * Set the id of the user that owns this metadata.
     *
     * @param owner the id of the user that owns this metadata.
     */
    public MetadataSourceInfo setOwner(Integer owner) {
        this._owner = owner;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataSourceInfo that = (MetadataSourceInfo) o;

        if (_groupOwner != null ? !_groupOwner.equals(that._groupOwner) : that._groupOwner != null)
            return false;
        if (_owner != null ? !_owner.equals(that._owner) : that._owner != null) return false;
        if (_sourceId != null ? !_sourceId.equals(that._sourceId) : that._sourceId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _sourceId != null ? _sourceId.hashCode() : 0;
        result = 31 * result + (_groupOwner != null ? _groupOwner.hashCode() : 0);
        result = 31 * result + (_owner != null ? _owner.hashCode() : 0);
        return result;
    }

	@Override
	public String toString() {
		return "MetadataSourceInfo [" + (_sourceId != null ? "_sourceId=" + _sourceId + ", " : "")
				+ (_groupOwner != null ? "_groupOwner=" + _groupOwner + ", " : "")
				+ (_owner != null ? "_owner=" + _owner : "") + "]";
	}
	
	@Override
	protected MetadataSourceInfo clone() {
		MetadataSourceInfo clon = new MetadataSourceInfo();
		
		clon.setGroupOwner(this.getGroupOwner());
		clon.setOwner(this.getOwner());
		clon.setSourceId(this.getSourceId());
		
		return clon;
	}
}
