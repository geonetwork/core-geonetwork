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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;

/**
 * A user feedback associated to a metadata (n:1) record or to another user feedback (n:1).
 */
@Entity(name = "GUF_UserFeedback")
@Table(name = "GUF_UserFeedbacks")
public class UserFeedback extends GeonetEntity implements Serializable {


    public enum UserRatingStatus {
        PUBLISHED,
        WAITING_FOR_APPROVAL;
    }


    private static final long serialVersionUID = -5537639171291203188L;

    private String uuid;
    private String commentText;
    private List<Rating> detailedRatingList;
    private Metadata metadata;
    private UserFeedback parent;
    private User authorId;
    private String authorName;
    private String authorOrganization;
    private String authorEmail;
    private int authorPrivacy;
    private Set<Keyword> keywords;
    private UserRatingStatus status;
    private User approver;
    private Date creationDate;


    @Id
    public String getUuid() {
        return uuid;
    }


    @Column
    public String getCommentText() {
        return commentText;
    }


    @OneToMany(cascade = CascadeType.ALL)
    public List<Rating> getDetailedRatingList() {
        return detailedRatingList;
    }


    @ManyToOne
    @JoinColumn(name = "metadata_uuid", referencedColumnName = "uuid")
    public Metadata getMetadata() {
        return metadata;
    }


    @ManyToOne
    @JoinColumn(name = "parent_uuid", referencedColumnName = "uuid")
    public UserFeedback getParent() {
        return parent;
    }


    @ManyToOne
    @Nullable
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    public User getAuthorId() {
        return authorId;
    }


    @Column
    @Nullable
    public String getAuthorName() {
        return authorName;
    }


    @Column
    @Nullable
    public String getAuthorOrganization() {
        return authorOrganization;
    }


    @Column
    @Nullable
    public String getAuthorEmail() {
        return authorEmail;
    }


    @Column
    @Nullable
    public int getAuthorPrivacy() {
        return authorPrivacy;
    }


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "GUF_userfeedback_keyword", joinColumns = @JoinColumn(name = "userfeedback_uuid", referencedColumnName = "uuid"), inverseJoinColumns = @JoinColumn(name = "keyword_id", referencedColumnName = "id"))
    public Set<Keyword> getKeywords() {
        return keywords;
    }


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public UserRatingStatus getStatus() {
        return status;
    }


    @ManyToOne
    @Nullable
    @JoinColumn(name = "approver_id", referencedColumnName = "id")
    public User getApprover() {
        return approver;
    }


    @Column
    public Date getCreationDate() {
        return creationDate;
    }


    public void setUuid(String uuid) {
        if (uuid == null || uuid.equals("")) {
            this.uuid = UUID.randomUUID().toString();
        } else {
            this.uuid = uuid;
        }
    }


    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }


    public void setDetailedRatingList(List<Rating> detailedRatingList) {
        this.detailedRatingList = detailedRatingList;
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }


    public void setParent(UserFeedback parent) {
        this.parent = parent;
    }


    public void setAuthorId(User authorId) {
        this.authorId = authorId;
    }


    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }


    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }


    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }


    public void setAuthorPrivacy(int authorPrivacy) {
        this.authorPrivacy = authorPrivacy;
    }


    public void setKeywords(Set<Keyword> keywords) {
        this.keywords = keywords;
    }


    public void setStatus(UserRatingStatus status) {
        this.status = status;
    }


    public void setApprover(User approver) {
        this.approver = approver;
    }


    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

        final UserFeedback rhs = (UserFeedback) obj;
        return uuid == null ? false : uuid.equals(rhs.uuid);
    }


    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += uuid == null ? 0 : uuid.hashCode() * 31;
        return hashCode;
    }


    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getUuid());
    }

}
