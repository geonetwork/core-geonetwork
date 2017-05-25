package org.fao.geonet.domain.userfeedback;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;

@Entity(name = "UserFeedback_Citation")
@Table(name = "UserFeedback_Citation")
public class Citation extends GeonetEntity {
    
    @Id
    private String uuid;
    
    @Column
    private String title;
    
    @Column
    private Date date;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }



}
