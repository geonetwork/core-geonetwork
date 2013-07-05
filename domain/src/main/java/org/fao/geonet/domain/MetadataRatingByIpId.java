package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;

/**
 * An Id object for {@link MetadataRatingByIp}
 * @author Jesse
 *
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataRatingByIpId implements Serializable {

    private static final long serialVersionUID = 2793801901676171677L;
    private int _metadataId;
    private String _ipAddress;

    @Column(name="metadataid", nullable=false)
    public int getMetadataId() {
        return _metadataId;
    }

    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }

    @Column(name="ipaddress", nullable=false, length=Constants.IP_ADDRESS_COLUMN_LENGTH)
    public String getIpAddress() {
        return _ipAddress;
    }

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
