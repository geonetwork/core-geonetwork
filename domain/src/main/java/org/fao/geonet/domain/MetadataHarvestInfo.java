package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Encapsulates the harvest data related to a metadata document. Like whether the metadata was harvested, the uuid of the harvester, etc...
 * This is a JPA Embeddable object that is embedded into a {@link Metadata} Entity
 * 
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataHarvestInfo {
    private char _harvested = 'n';
    private String _uuid;
    private String _uri;

    /**
     * For backwards compatibility we need the isharvested column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "isharvested", length = 1, nullable = false)
    protected char isHarvested_JPAWorkaround() {
        return _harvested;
    }

    /**
     * Set the code for the harvested column.
     * 
     * @param harvested 'y' or 'n'
     */
    protected void setHarvested_JPAWorkaround(char harvested) {
        this._harvested = harvested;
    }

    /**
     * Return true if the metadata was harvested.
     * 
     * @return true if the metadata was harvested.
     */
    @Transient
    public boolean isHarvested() {
        return _harvested == 'y';
    }

    /**
     * true if the metadata was harvested, false otherwise.
     * 
     * @param harvested true if the metadata was harvested.
     * @return this data info object
     */
    public MetadataHarvestInfo setHarvested(boolean harvested) {
        this._harvested = harvested ? 'y' : 'n';
        return this;
    }

    /**
     * Get the uuid of the harvester that harvested this metadata (if the metadata is harvested metadata)
     * 
     * @return the uuid of the harvester that harvested this metadata (if the metadata is harvested metadata)
     * @see #isHarvested()
     */
    @Column(name = "harvestuuid")
    public String getUuid() {
        return _uuid;
    }

    /**
     * Set the uuid of the harvester that harvested this metadata (if the metadata is harvested metadata)
     * 
     * @param uuid the uuid of the harvester that harvested this metadata (if the metadata is harvested metadata)
     * @see #isHarvested()
     */
    public void setUuid(String uuid) {
        this._uuid = uuid;
    }

    /**
     * Get the optional uri indicating what was harvested to get this metadata.
     * 
     * @return the optional uri indicating what was harvested to get this metadata.
     */
    @Column(name = "harvesturi", length = 512)
    public String getUri() {
        return _uri;
    }

    /**
     * Set the optional uri indicating what was harvested to get this metadata.
     * 
     * @param uri the optional uri indicating what was harvested to get this metadata.
     * @return this data info object
     */
    public MetadataHarvestInfo setUri(String uri) {
        this._uri = uri;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _harvested;
        result = prime * result + ((_uri == null) ? 0 : _uri.hashCode());
        result = prime * result + ((_uuid == null) ? 0 : _uuid.hashCode());
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
        MetadataHarvestInfo other = (MetadataHarvestInfo) obj;
        if (_harvested != other._harvested)
            return false;
        if (_uri == null) {
            if (other._uri != null)
                return false;
        } else if (!_uri.equals(other._uri))
            return false;
        if (_uuid == null) {
            if (other._uuid != null)
                return false;
        } else if (!_uuid.equals(other._uuid))
            return false;
        return true;
    }
}
