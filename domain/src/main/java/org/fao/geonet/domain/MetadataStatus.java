package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An entity that represents a status change of a metadata.
 * 
 * Note:  I am not the author of metadata status, but it appears that
 * this tracks the history as well since the Id consists of the User, date, metadata
 * and statusvalue of the metadata status change.
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
    
    @EmbeddedId
    public MetadataStatusId getId() {
        return id;
    }
    public void setId(MetadataStatusId id) {
        this.id = id;
    }
    public String getChangeMessage() {
        return changeMessage;
    }
    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }
    
    
}
