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
package org.fao.geonet.domain.userfeedback;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * One single aspect that could be evalueted in a user feedback: it's a (n:1) relation with a User feedback (so not directly related to
 * metadata)
 */
@Entity(name = "GUF_Rating")
@Table(name = "GUF_Rating")
@SequenceGenerator(name = Rating.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Rating implements Serializable {

    /** Sequence name */
    static final String ID_SEQ_NAME = "gufrat_id_seq";

    private static final long serialVersionUID = -3085407373602831420L;


    private long id;
    private UserFeedback userfeedback;
    private RatingCriteria category;
    private Integer rating;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Rating.ID_SEQ_NAME)
    public long getId() {
        return id;
    }


    @ManyToOne
    @JoinColumn(name = "userfeedback_id", referencedColumnName = "uuid")
    public UserFeedback getUserfeedback() {
        return userfeedback;
    }


    @ManyToOne
    @JoinColumn(name = "criteria_id", referencedColumnName = "id")
    public RatingCriteria getCategory() {
        return category;
    }


    @Column
    public Integer getRating() {
        return rating;
    }


    public void setId(long id) {
        this.id = id;
    }


    public void setUserfeedback(UserFeedback userfeedback) {
        this.userfeedback = userfeedback;
    }


    public void setCategory(RatingCriteria category) {
        this.category = category;
    }


    public void setRating(Integer rating) {
        this.rating = rating;
    }


}
