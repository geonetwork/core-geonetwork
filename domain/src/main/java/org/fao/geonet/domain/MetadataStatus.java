package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An entity that represents a status change of a metadata.
 * 
 * Note: I am not the author of metadata status, but it appears that this tracks the history as well since the Id consists of the User,
 * date, metadata and statusvalue of the metadata status change.
 * 
 * @author Jesse
 * 
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "metadatastatus")
public class MetadataStatus {
    private MetadataStatusId id;
    private String changeMessage;

    /**
     * Get the id object of this metadata status object.
     * 
     * @return the id object of this metadata status object.
     */
    @EmbeddedId
    public MetadataStatusId getId() {
        return id;
    }

    /**
     * Set the id object of this metadata status object.
     * 
     * @param id the id object of this metadata status object.
     */
    public void setId(MetadataStatusId id) {
        this.id = id;
    }

    /**
     * Get the change message, the message that describes the change in status. It is application specific.
     * 
     * @return the change message
     */
    @Column(length = 2048, nullable = false)
    public String getChangeMessage() {
        return changeMessage;
    }

    /**
     * Set the change message, the message that describes the change in status. It is application specific.
     * 
     * @param changeMessage the change message
     */
    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }

}
