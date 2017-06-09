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
import javax.persistence.Table;

@Entity(name = "GUF_Rating")
@Table(name = "GUF_Rating")
public class Rating implements Serializable {

    public enum Category {
        AVG, COMPLETE, READABILITY, FINDABILITY, OTHER;
    }

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

    public Category getCategory() {
        return category;
    }

    public long getId() {
        return id;
    }

    public Integer getRating() {

        return rating;
    }

    public UserFeedback getUserfeedback() {
        return userfeedback;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setUserfeedback(UserFeedback userfeedback) {
        this.userfeedback = userfeedback;
    }

}
