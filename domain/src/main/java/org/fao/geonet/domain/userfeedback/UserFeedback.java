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

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;

@Entity(name = "UserFeedback")
@Table(name = "UserFeedback")
public class UserFeedback extends GeonetEntity {
    
    @Id
    private String uuid;

    @Column
    private String comment;

    @ManyToOne
    private Metadata metadata;

    @ManyToOne
    private UserFeedback parent;

    @ManyToOne
    private User user;

    @ManyToMany
    private Set<Keyword> keyword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRatingStatus status;

    @ManyToOne
    private User approver;

    @Column(nullable = false)
    private Date date;

    @OneToOne
    private Citation citation;

    public UserFeedback() {
        this.uuid = UUID.randomUUID().toString();
    }    
   
    enum UserRatingStatus {
        PUBLISHED, WAITING_FOR_APPROVAL;
    } 
    
    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getUuid());
    }


    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        UserFeedback rhs = (UserFeedback) obj;
        return this.uuid == null ? false : this.uuid.equals(rhs.uuid);
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += (this.uuid == null) ? 0 : this.uuid.hashCode() * 31;
        return hashCode;
    }


    public String getUuid() {
        return uuid;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public Metadata getMetadata() {
        return metadata;
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }


    public UserFeedback getParent() {
        return parent;
    }


    public void setParent(UserFeedback parent) {
        this.parent = parent;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public Set<Keyword> getKeyword() {
        return keyword;
    }


    public void setKeyword(Set<Keyword> keyword) {
        this.keyword = keyword;
    }


    public UserRatingStatus getStatus() {
        return status;
    }


    public void setStatus(UserRatingStatus status) {
        this.status = status;
    }


    public User getApprover() {
        return approver;
    }


    public void setApprover(User approver) {
        this.approver = approver;
    }


    public Date getDate() {
        return date;
    }


    public void setDate(Date date) {
        this.date = date;
    }


    public Citation getCitation() {
        return citation;
    }


    public void setCitation(Citation citation) {
        this.citation = citation;
    }
    
    

}
