package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Information about the source and owner of the metadata document. This is a JPA Embeddable object that is embedded into a {@link
 * Metadata}
 * Entity
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataSourceInfo {
    private String _sourceId;
    private Integer _groupOwner;
    private int _owner;

    /**
     * Get the source of the metadata. The source identifies where the metadata came from. It is usually a uuid but can be any identifier.
     * Normally if the metadata is harvested this will be harvester uuid and if it is locally created it will typically be the siteid
     * of the
     * geonetwork instance.
     *
     * @return the source of the metadata.
     */
    @Column(name = "source", nullable = false)
    public String getSourceId() {
        return _sourceId;
    }

    /**
     * Set the source of the metadata. The source identifies where the metadata came from. It is usually a uuid but can be any identifier.
     * Normally if the metadata is harvested this will be harvester uuid and if it is locally created it will typically be the siteid
     * of the
     * geonetwork instance.
     *
     * @param sourceId the source of the metadata.
     */
    public MetadataSourceInfo setSourceId(String sourceId) {
        this._sourceId = sourceId;
        return this;
    }

    /**
     * Get the group id that owns this metadata. A user can be part of several groups thus the group owner determines which one of those
     * groups the metadata belongs to.
     *
     * @return the group that owns this metadata.
     */
    @Column(name = "groupOwner")
    public Integer getGroupOwner() {
        return _groupOwner;
    }

    /**
     * Set the group id that owns this metadata. A user can be part of several groups thus the group owner determines which one of those
     * groups the metadata belongs to.
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
    public int getOwner() {
        return _owner;
    }

    /**
     * Set the id of the user that owns this metadata.
     *
     * @param owner the id of the user that owns this metadata.
     */
    public MetadataSourceInfo setOwner(int owner) {
        this._owner = owner;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _groupOwner;
        result = prime * result + _owner;
        result = prime * result + ((_sourceId == null) ? 0 : _sourceId.hashCode());
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
        MetadataSourceInfo other = (MetadataSourceInfo) obj;
        if (!_groupOwner.equals(other._groupOwner))
            return false;
        if (_owner != other._owner)
            return false;
        if (_sourceId == null) {
            if (other._sourceId != null)
                return false;
        } else if (!_sourceId.equals(other._sourceId))
            return false;
        return true;
    }
}
