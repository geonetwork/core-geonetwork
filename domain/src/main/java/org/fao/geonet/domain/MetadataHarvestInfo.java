package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Encapsulates the harvest data related to a
 * metadata document.  Like whether the metadata
 * was harvested, the uuid of the harvester, etc...
 * This is a JPA Embeddable object that is embedded into a {@link Metadata} Entity
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataHarvestInfo {
    private boolean _harvested;
    private String _uuid;
    private String _uri;

    @Column(name="isharvested")
    public boolean isHarvested() {
        return _harvested;
    }
    public void setHarvested(boolean harvested) {
        this._harvested = harvested;
    }
    @Column(name="harvestuuid")
    public String getUuid() {
        return _uuid;
    }
    public void setUuid(String uuid) {
        this._uuid = uuid;
    }
    @Column(name="harvesturi")
    public String getUri() {
        return _uri;
    }
    public void setUri(String uri) {
        this._uri = uri;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_harvested ? 1231 : 1237);
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
