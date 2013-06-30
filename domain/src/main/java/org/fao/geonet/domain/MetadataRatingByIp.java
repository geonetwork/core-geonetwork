package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An entity that tracks which users have rated a metadata.  
 * It currently tracks by Ip address so that each IP address can
 * only rate a given metadata once.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "metadatarating")
public class MetadataRatingByIp {
    private MetadataRatingByIpId _id;
    private int _rating;
    
    @EmbeddedId
    public MetadataRatingByIpId getId() {
        return _id;
    }
    public void setId(MetadataRatingByIpId id) {
        this._id = id;
    }
    @Column(nullable=false)
    public int getRating() {
        return _rating;
    }
    public void setRating(int rating) {
        this._rating = rating;
    }
    
    
}
