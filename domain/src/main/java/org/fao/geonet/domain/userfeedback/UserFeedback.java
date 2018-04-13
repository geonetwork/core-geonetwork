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
import javax.persistence.OneToOne;
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

    /**
     * Comments made by external users should be approved.
     */
    public enum UserRatingStatus {

        /** The published. */
        PUBLISHED,
        /** The waiting for approval. */
        WAITING_FOR_APPROVAL;
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5537639171291203188L;

    /** The uuid. */
    @Id
    private String uuid;

    /** The comment. */
    @Column
    private String comment;

    /** The relation between a user feedback and the ratings. */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Rating> detailedRatingList;

    /** The metadata. */
    @ManyToOne
    @JoinColumn(name = "metadata_uuid", referencedColumnName = "uuid")
    private Metadata metadata;

    /** The parent. */
    @ManyToOne
    @JoinColumn(name = "parent_uuid", referencedColumnName = "uuid")
    private UserFeedback parent;

    /** The author id. */
    @ManyToOne
    @Nullable
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User authorId;

    /** The author name. */
    @Column
    @Nullable
    private String authorName;

    /** The author organization. */
    @Column
    @Nullable
    private String authorOrganization;

    /** The author email. */
    @Column
    @Nullable
    private String authorEmail;

    /** The author privacy. */
    @Column
    @Nullable
    private int authorPrivacy;

    /** The keywords. */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "GUF_userfeedback_keyword", joinColumns = @JoinColumn(name = "userfeedback_uuid", referencedColumnName = "uuid"), inverseJoinColumns = @JoinColumn(name = "keyword_id", referencedColumnName = "id"))
    private Set<Keyword> keywords;

    /** The status. */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRatingStatus status;

    /** The approver. */
    @ManyToOne
    @Nullable
    @JoinColumn(name = "approver_id", referencedColumnName = "id")
    private User approver;

    /** The date. */
    @Column(nullable = false)
    private Date date;

    /** The citation. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "citation_id", referencedColumnName = "id")
    private Citation citation;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /**
     * Gets the approver.
     *
     * @return the approver
     */
    public User getApprover() {
        return approver;
    }

    /**
     * Gets the author email.
     *
     * @return the author email
     */
    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * Gets the author id.
     *
     * @return the author id
     */
    public User getAuthorId() {
        return authorId;
    }

    /**
     * Gets the author name.
     *
     * @return the author name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Gets the author organization.
     *
     * @return the author organization
     */
    public String getAuthorOrganization() {
        return authorOrganization;
    }

    /**
     * Gets the author privacy.
     *
     * @return the author privacy
     */
    public int getAuthorPrivacy() {
        return authorPrivacy;
    }

    /**
     * Gets the citation.
     *
     * @return the citation
     */
    public Citation getCitation() {
        return citation;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the detailed rating list.
     *
     * @return the detailed rating list
     */
    public List<Rating> getDetailedRatingList() {
        return detailedRatingList;
    }

    /**
     * Gets the keywords.
     *
     * @return the keywords
     */
    public Set<Keyword> getKeywords() {
        return keywords;
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public UserFeedback getParent() {
        return parent;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public UserRatingStatus getStatus() {
        return status;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += uuid == null ? 0 : uuid.hashCode() * 31;
        return hashCode;
    }

    /**
     * Sets the approver.
     *
     * @param approver the new approver
     */
    public void setApprover(User approver) {
        this.approver = approver;
    }

    /**
     * Sets the author email.
     *
     * @param authorEmail the new author email
     */
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    /**
     * Sets the author id.
     *
     * @param authorId the new author id
     */
    public void setAuthorId(User authorId) {
        this.authorId = authorId;
    }

    /**
     * Sets the author name.
     *
     * @param authorName the new author name
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Sets the author organization.
     *
     * @param authorOrganization the new author organization
     */
    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    /**
     * Sets the author privacy.
     *
     * @param authorPrivacy the new author privacy
     */
    public void setAuthorPrivacy(int authorPrivacy) {
        this.authorPrivacy = authorPrivacy;
    }

    /**
     * Sets the citation.
     *
     * @param citation the new citation
     */
    public void setCitation(Citation citation) {
        this.citation = citation;
    }

    /**
     * Sets the comment.
     *
     * @param comment the new comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the detailed rating list.
     *
     * @param detailedRatingList the new detailed rating list
     */
    public void setDetailedRatingList(List<Rating> detailedRatingList) {
        this.detailedRatingList = detailedRatingList;
    }

    /**
     * Sets the keywords.
     *
     * @param keywords the new keywords
     */
    public void setKeywords(Set<Keyword> keywords) {
        this.keywords = keywords;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata the new metadata
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the parent.
     *
     * @param parent the new parent
     */
    public void setParent(UserFeedback parent) {
        this.parent = parent;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(UserRatingStatus status) {
        this.status = status;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid the new uuid
     */
    public void setUuid(String uuid) {
        if (uuid == null || uuid.equals("")) {
            this.uuid = UUID.randomUUID().toString();
        } else {
            this.uuid = uuid;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getUuid());
    }

}
