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

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * An Id object for {@link MetadataRatingByIp}
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataRatingByIpId implements Serializable {

    private static final long serialVersionUID = 2793801901676171677L;
    private int _metadataId;
    private String _ipAddress;

    /**
     * Default constructor used by JPA framework.
     */
    public MetadataRatingByIpId() {
        // default constructor for JPA construction.
    }

    /**
     * Convenience constructor.
     *
     * @param metatatId the metadata id that is being rated.
     * @param ipAddress the id of the user making the rating.
     */
    public MetadataRatingByIpId(int metatatId, String ipAddress) {
        this._metadataId = metatatId;
        this._ipAddress = ipAddress;
    }

    /**
     * Get the id of the associated metadata.
     *
     * @return the id of the associated metadata.
     */
    @Column(name = "metadataId", nullable = false)
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the id of the associated metadata.
     *
     * @param metadataId the id of the associated metadata.
     */
    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }

    /**
     * Get the IP Address of the user the rating is related to.
     *
     * @return the IP Address of the user the rating is related to.
     */
    @Column(name = "ipAddress", nullable = false, length = Constants.IP_ADDRESS_COLUMN_LENGTH)
    public String getIpAddress() {
        return _ipAddress;
    }

    /**
     * Set the IP Address of the user the rating is related to.
     *
     * @param ipAddress the IP Address of the user the rating is related to.
     */
    public void setIpAddress(String ipAddress) {
        this._ipAddress = ipAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_ipAddress == null) ? 0 : _ipAddress.hashCode());
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
        MetadataRatingByIpId other = (MetadataRatingByIpId) obj;
        if (_ipAddress == null) {
            if (other._ipAddress != null)
                return false;
        } else if (!_ipAddress.equals(other._ipAddress))
            return false;
        if (_metadataId != other._metadataId)
            return false;
        return true;
    }

}
