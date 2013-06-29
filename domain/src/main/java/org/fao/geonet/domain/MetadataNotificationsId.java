package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataNotificationsId implements Serializable {
    private static final long serialVersionUID = 8167301479650105617L;
    private int metadataId;
    private int notifierId;
    public int getMetadataId() {
        return metadataId;
    }
    public void setMetadataId(int metadataId) {
        this.metadataId = metadataId;
    }
    public int getNotifierId() {
        return notifierId;
    }
    public void setNotifierId(int notifierId) {
        this.notifierId = notifierId;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + metadataId;
        result = prime * result + notifierId;
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
        MetadataNotificationsId other = (MetadataNotificationsId) obj;
        if (metadataId != other.metadataId)
            return false;
        if (notifierId != other.notifierId)
            return false;
        return true;
    }
}
