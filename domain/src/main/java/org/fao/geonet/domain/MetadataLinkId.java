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

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * The id object of {@link MetadataLink}
 */
@Embeddable
public class MetadataLinkId implements Serializable {
    private static final long serialVersionUID = -5759713154514715316L;

    private int _metadataId;
    private int _linkId;

    /**
     * Default constructor. Setters must be used to initialize object.
     */
    public MetadataLinkId() {
        // default constructor.
    }

    /**
     * Create a new instance from the required id objects.
     *
     * @param metadataId  the metadata id
     * @param linkId     the link id
     */
    public MetadataLinkId(int metadataId, int linkId) {
        this._metadataId = metadataId;
        this._linkId = _linkId;
    }

    /**
     * Get the id of the metadata this MetadataLink is references to.
     *
     * @return the id of the metadata this MetadataLink is references to.
     */
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the id of the metadata this MetadataLink is references to.
     *
     * @param newMetadataId the id of the metadata this MetadataLink is references to.
     * @return this id object
     */
    public MetadataLinkId setMetadataId(int newMetadataId) {
        this._metadataId = newMetadataId;
        return this;
    }

    /**
     * Get the id of the group this MetadataLink is references to.
     *
     * @return the id of the group this MetadataLink is references to.
     */
    public int getLinkId() {
        return _linkId;
    }

    /**
     * Get the id of the group this MetadataLink is references to.
     *
     * @param newLinkId the id of the group this MetadataLink is references to.
     * @return this id object
     */
    public MetadataLinkId setLinkId(int newLinkId) {
        this._linkId = newLinkId;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _linkId;
        result = prime * result + _metadataId;
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
        MetadataLinkId other = (MetadataLinkId) obj;
        if (_linkId != other._linkId)
            return false;
        if (_metadataId != other._metadataId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MetadataLinkId [metadataId=" + _metadataId + ", linkId=" + _linkId + "]";
    }

    /**
     * Make a copy of this id object.
     *
     * @return a copy of this id object.
     */
    public MetadataLinkId copy() {
        MetadataLinkId copy = new MetadataLinkId();
        copy._linkId = _linkId;
        copy._metadataId = _metadataId;
        return copy;
    }
}
