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

    public int getMetadataId() {
        return _metadataId;
    }

    public void setMetadataId(int metadataid) {
        this._metadataId = metadataid;
    }

    @Column(name = "valtype", length = 40)
    public String getValidationType() {
        return _validationType;
    }

    public void setValidationType(String validationType) {
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
}
