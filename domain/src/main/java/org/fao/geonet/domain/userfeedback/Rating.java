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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * One single aspect that could be evalueted in a user feedback: it's a (n:1) relation with a User feedback (so not directly related to
 * metadata)
 */
@Entity(name = "GUF_Rating")
@Table(name = "GUF_Rating")
@SequenceGenerator(name = Rating.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Rating implements Serializable {

    /**
     * The Enum Category.
     */
    public enum Category {

        /** The avg. */
        AVG,
        /** The complete. */
        COMPLETE,
        /** The readability. */
        READABILITY,
        /** The findability. */
        FINDABILITY,
        /** The dataquality. */
        DATAQUALITY,
        /** The servicequality. */
        SERVICEQUALITY,
        /** The other. */
        OTHER;
    }

    /** The Constant ID_SEQ_NAME. */
    static final String ID_SEQ_NAME = "gufrat_id_seq";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3085407373602831420L;

    /** The id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = Rating.ID_SEQ_NAME)
    private long id;

    /** The userfeedback. */
    @ManyToOne
    @JoinColumn(name = "userfeedback_id", referencedColumnName = "uuid")
    private UserFeedback userfeedback;

    /** The category. */
    @Column
    @Enumerated(EnumType.STRING)
    private Category category;

    /** The rating. */
    @Column
    private Integer rating;

    /**
     * Gets the category.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the rating.
     *
     * @return the rating
     */
    public Integer getRating() {

        return rating;
    }

    /**
     * Gets the userfeedback.
     *
     * @return the userfeedback
     */
    public UserFeedback getUserfeedback() {
        return userfeedback;
    }

    /**
     * Sets the category.
     *
     * @param category the new category
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Sets the rating.
     *
     * @param rating the new rating
     */
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    /**
     * Sets the userfeedback.
     *
     * @param userfeedback the new userfeedback
     */
    public void setUserfeedback(UserFeedback userfeedback) {
        this.userfeedback = userfeedback;
    }

}
