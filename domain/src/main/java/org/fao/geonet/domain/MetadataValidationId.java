/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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
 * Id object for the {@link MetadataValidation} entity.
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataValidationId implements Serializable {
    private static final long serialVersionUID = -7162983572434017017L;
    public static final String METADATA_ID_COLUMN_NAME = "metadataId";
    public static final String VALIDATION_TYPE_COLUMN_NAME = "valType";
    private int _metadataId;
    private String _validationType;

    /**
     * Default constructor.
     */
    public MetadataValidationId() {
    }

    /**
     * Convenience constructor.
     *
     * @param metadataId     the metadata id
     * @param validationType the validation type
     */
    public MetadataValidationId(final int metadataId, final String validationType) {
        this._metadataId = metadataId;
        this._validationType = validationType;
    }

    /**
     * Get the id of the associate metadata.
     */
    @Column(name = METADATA_ID_COLUMN_NAME)
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the metadata id.
     *
     * @param metadataid the metadata id
     */
    public void setMetadataId(final int metadataid) {
        this._metadataId = metadataid;
    }

    /**
     * Get a string representing the type of validation of this validation entity. (example:
     * iso19139)
     *
     * @return a string representing the type of validation of this validation entity (example:
     * iso19139)
     */
    @Column(name = VALIDATION_TYPE_COLUMN_NAME, length = 128)
    public String getValidationType() {
        return _validationType;
    }

    /**
     * Set a string representing the type of validation of this validation entity. (example:
     * iso19139)
     *
     * @param validationType a string representing the type of validation of this validation entity
     *                       (example: iso19139)
     */
    public void setValidationType(final String validationType) {
        this._validationType = validationType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _metadataId;
        result = prime * result + ((_validationType == null) ? 0 : _validationType.hashCode());
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
        MetadataValidationId other = (MetadataValidationId) obj;
        if (_metadataId != other._metadataId)
            return false;
        if (_validationType == null) {
            if (other._validationType != null)
                return false;
        } else if (!_validationType.equals(other._validationType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + _metadataId + ", " + _validationType + "]";
    }
}
