package org.fao.geonet.domain.userfeedback;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;

@Entity(name = "UserFeedback_Keyword")
@Table(name = "UserFeedback_Keyword")
public class Keyword extends GeonetEntity {
    
    @Id
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
