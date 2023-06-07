/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.model;


import org.fao.geonet.domain.ISODate;

/**
 * Information used for metadata publication/un-publication mail notifications.
 */
public class MetadataPublicationNotificationInfo {
    private String metadataUuid;
    private int metadataId;
    private Integer groupId;
    private Boolean published;
    private ISODate publicationDateStamp;
    private String publisherUser = "";

    private String reviewerUser = "";
    private String submitterUser = "";

    /**
     * When the metadata workflow is enabled and the metadata was already
     * published, but has been reapproved a working copy. To use a different
     * message for the mail.
     */
    private boolean reapproval = false;

    public String getMetadataUuid() {
        return metadataUuid;
    }

    public void setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
    }

    public int getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(int metadataId) {
        this.metadataId = metadataId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public ISODate getPublicationDateStamp() {
        return publicationDateStamp;
    }

    public void setPublicationDateStamp(ISODate publicationDateStamp) {
        this.publicationDateStamp = publicationDateStamp;
    }

    public String getPublisherUser() {
        return publisherUser;
    }

    public void setPublisherUser(String publisherUser) {
        this.publisherUser = publisherUser;
    }

    public String getReviewerUser() {
        return reviewerUser;
    }

    public void setReviewerUser(String reviewerUser) {
        this.reviewerUser = reviewerUser;
    }

    public String getSubmitterUser() {
        return submitterUser;
    }

    public void setSubmitterUser(String submitterUser) {
        this.submitterUser = submitterUser;
    }

    public boolean isReapproval() {
        return reapproval;
    }

    public void setReapproval(boolean reapproval) {
        this.reapproval = reapproval;
    }
}
