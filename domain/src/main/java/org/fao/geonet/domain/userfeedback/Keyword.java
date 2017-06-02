package org.fao.geonet.domain.userfeedback;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;

@Entity(name = "GUF_Keywords")
@Table(name = "GUF_Keywords")
public class Keyword extends GeonetEntity implements Serializable {
    
    private static final long serialVersionUID = -5828055384917117874L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @Column    
    private String value;
    
    @ManyToMany(mappedBy = "keywords")
    private List<UserFeedback> userfeedbacks;   
    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<UserFeedback> getUserfeedbacks() {
        return userfeedbacks;
    }

    public void setUserfeedbacks(List<UserFeedback> userfeedbacks) {
        this.userfeedbacks = userfeedbacks;
    }
    
}
