package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Id of a MetadataNotification object.
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataNotificationId implements Serializable {
    private static final long serialVersionUID = 8167301479650105617L;
    private int metadataId;
    private int notifierId;

    /**
     * Get the id of the metadata the notification is related to.
     *
     * @return the id of the metadata the notification is related to.
     */
    public int getMetadataId() {
        return metadataId;
    }

    /**
     * Set the id of the metadata the notification is related to.
     *
     * @param metadataId the id of the metadata the notification is related to.
     * @return this id object
     */
    public MetadataNotificationId setMetadataId(int metadataId) {
        this.metadataId = metadataId;
        return this;
    }

    /**
     * Get the id of the notifier (notification listener).
     *
     * @return the id of the notifier (notification listener)
     * @see MetadataNotifier
     */
    public int getNotifierId() {
        return notifierId;
    }

    /**
     * Set the id of the notifier (notification listener).
     *
     * @param notifierId the id of the notifier (notification listener)
     * @return the id of the notifier (notification listener)
     * @see MetadataNotifier
     */
    public MetadataNotificationId setNotifierId(int notifierId) {
        this.notifierId = notifierId;
        return this;
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
        MetadataNotificationId other = (MetadataNotificationId) obj;
        if (metadataId != other.metadataId)
            return false;
        if (notifierId != other.notifierId)
            return false;
        return true;
    }
}
