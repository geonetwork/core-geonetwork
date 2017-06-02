package org.fao.geonet.domain.userfeedback;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;

@Entity(name = "GUF_Citation")
@Table(name = "GUF_Citation")
public class Citation extends GeonetEntity  implements Serializable {
    
    private static final long serialVersionUID = -5575769991362660985L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column
    private String title;
    
    @Column
    private Date date;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
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
