package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
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
