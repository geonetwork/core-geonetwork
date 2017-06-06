package org.fao.geonet.domain.userfeedback;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "GUF_Rating")
@Table(name = "GUF_Rating")
public class Rating implements Serializable {
 
    private static final long serialVersionUID = -3085407373602831420L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    @ManyToOne
    @JoinColumn(name = "userfeedback_id", referencedColumnName = "uuid")
    private UserFeedback userfeedback;
    
    @Column
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @Column
    private Integer rating;
    
    public enum Category {
        AVG, COMPLETE, READABILITY, FINDABILITY, OTHER;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }   

    public UserFeedback getUserfeedback() {
        return userfeedback;
    }

    public void setUserfeedback(UserFeedback userfeedback) {
        this.userfeedback = userfeedback;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

  
}
