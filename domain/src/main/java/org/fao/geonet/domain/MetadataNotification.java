package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    private MetadataNotificationId _id;
    private char _notified = 'n';
    private String _metadataUuid;
    private char _action;
    private String _errorMessage;

    @EmbeddedId
    public MetadataNotificationId getId() {
        return _id;
    }

    public void setId(MetadataNotificationId id) {
        this._id = id;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="notified", length=1, nullable=false)
    public char isNotified_JPAWorkaround() {
        return _notified;
    }
    public void setNotified_JPAWorkaround(char notified) {
        this._notified = notified;
    }
    @Transient
    public boolean isNotified() {
        return _notified == 'y';
    }

    public void setNotified(boolean notified) {
        this._notified = notified ? 'y' : 'n';
    }

    @Column(name = "metadatauuid", nullable=false)
    public String getMetadataUuid() {
        return _metadataUuid;
    }

    public void setMetadataUuid(String metadataUuid) {
        this._metadataUuid = metadataUuid;
    }

    @Column(length=1, nullable=false)
    public char getAction() {
        return _action;
    }

    public void setAction(char action) {
        this._action = action;
    }

    @Lob
    @Column(name = "errormsg")
    public String getErrorMessage() {
        return _errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this._errorMessage = errorMessage;
    }

}
