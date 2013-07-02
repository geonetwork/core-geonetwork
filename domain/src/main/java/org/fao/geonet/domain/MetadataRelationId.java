package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Id class for Metadata relation.
 * 
 * @author Jesse
 */
@Embeddable
public class MetadataRelationId implements Serializable {
    private static final long serialVersionUID = -2705273953015744638L;

    private int _metadataId;
    private int _relatedId;

    public int getMetadataId() {
        return _metadataId;
    }

    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }

    public int getRelatedId() {
        return _relatedId;
    }

    public void setRelatedId(int relatedId) {
        this._relatedId = relatedId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _metadataId;
        result = prime * result + _relatedId;
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
        MetadataRelationId other = (MetadataRelationId) obj;
        if (_metadataId != other._metadataId)
            return false;
        if (_relatedId != other._relatedId)
            return false;
        return true;
    }

}
