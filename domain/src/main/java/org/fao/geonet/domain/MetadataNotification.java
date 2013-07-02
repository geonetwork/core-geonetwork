package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * An entity representing a metadata related notification that has been made or 
 * is pending.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "metadatanotifications")
public class MetadataNotification {
    private MetadataNotificationId id;
    private boolean notified;
    private String metadataUuid;
    private char action;
    private String errorMessage;

    @EmbeddedId
    public MetadataNotificationId getId() {
        return id;
    }

    public void setId(MetadataNotificationId id) {
        this.id = id;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Column(name = "metadatauuid")
    public String getMetadataUuid() {
        return metadataUuid;
    }

    public void setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
    }

    public char getAction() {
        return action;
    }

    public void setAction(char action) {
        this.action = action;
    }

    @Lob
    @Column(name = "errormessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
