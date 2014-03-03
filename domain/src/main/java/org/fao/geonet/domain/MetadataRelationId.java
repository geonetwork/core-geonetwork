package org.fao.geonet.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

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

    /**
     * Default constructor, needed by JPA.
     */
    public MetadataRelationId() {
    }

    /**
     * Convenience constructor.
     *
     * @param metadataId one side of the relation.
     * @param relatedId  the other side of relation.
     */
    public MetadataRelationId(Integer metadataId, Integer relatedId) {
        this._metadataId = metadataId;
        this._relatedId = relatedId;
    }

    /**
     * Get the id of the first metadata.
     *
     * @return the id of the first metadata.
     */
    @Column(name = "id")
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the id of the first metadata.
     *
     * @param metadataId the id of the first metadata.
     */
    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }

    /**
     * Get the id of the second metadata.
     *
     * @return the id of the second metadata.
     */
    @Column(name = "relatedId")
    public int getRelatedId() {
        return _relatedId;
    }

    /**
     * Set the id of the second metadata.
     *
     * @param relatedId the id of the second metadata.
     */
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
