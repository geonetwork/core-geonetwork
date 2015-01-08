package org.fao.geonet.domain;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Id object for the {@link MetadataValidation} entity.
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataValidationId implements Serializable {
    private static final long serialVersionUID = -7162983572434017017L;
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
     * Get a string representing the type of validation of this validation entity. (example: iso19139)
     *
     * @return a string representing the type of validation of this validation entity (example: iso19139)
     */
    @Column(name = "valType", length = 40)
    public String getValidationType() {
        return _validationType;
    }

    /**
     * Set a string representing the type of validation of this validation entity. (example: iso19139)
     *
     * @param validationType a string representing the type of validation of this validation entity (example: iso19139)
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
        return  "[" + _metadataId + ", " + _validationType + "]";
    }
}
