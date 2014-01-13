package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.MetadataRatingByIpEntityListenerManager;

import javax.persistence.*;

/**
 * An entity that tracks which users have rated a metadata. It currently tracks by Ip address so that each IP address can only rate a
 * given
 * metadata once.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataRating")
@EntityListeners(MetadataRatingByIpEntityListenerManager.class)
public class MetadataRatingByIp extends GeonetEntity {
    private MetadataRatingByIpId _id;
    private int _rating;

    /**
     * Get the id object of the metadata rating entity.
     *
     * @return the id object of the metadata rating entity.
     */
    @EmbeddedId
    public MetadataRatingByIpId getId() {
        return _id;
    }

    /**
     * Set the id object of the metadata rating entity.
     *
     * @param id the id object of the metadata rating entity.
     */
    public void setId(MetadataRatingByIpId id) {
        this._id = id;
    }

    /**
     * Get the rating for this IP address.
     *
     * @return the rating for this IP address.
     */
    @Column(nullable = false)
    public int getRating() {
        return _rating;
    }

    /**
     * Set the rating for this IP address.
     *
     * @param rating the rating for this IP address.
     */
    public void setRating(int rating) {
        this._rating = rating;
    }
}
