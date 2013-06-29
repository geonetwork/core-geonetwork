package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;


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
