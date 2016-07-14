/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.MetadataRatingByIpEntityListenerManager;

import javax.persistence.*;

/**
 * An entity that tracks which users have rated a metadata. It currently tracks by Ip address so
 * that each IP address can only rate a given metadata once.
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
