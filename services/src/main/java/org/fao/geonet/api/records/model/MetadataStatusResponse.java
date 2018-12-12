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

package org.fao.geonet.api.records.model;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId;
import org.fao.geonet.domain.StatusValue;

public class MetadataStatusResponse extends MetadataStatus {

    MetadataStatus metadataStatusObject;

    String authorName;
    String authorEmail;
    String authorProfile;

    String ownerName;
    String ownerEmail;
    String ownerProfile;

    String title;
    String currentStatus;
    String previousStatus;

    boolean loadFull;

    public MetadataStatusResponse(MetadataStatus s) {
        this.metadataStatusObject = s;
    }

    public MetadataStatusResponse(MetadataStatus s, boolean loadFull) {
        this.metadataStatusObject = s;
        if(loadFull) {
            this.loadFull = true;
            this.metadataStatusObject.getCurrentState();
            this.metadataStatusObject.getPreviousState();
        }
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public MetadataStatusResponse setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return this;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public MetadataStatusResponse setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
        return this;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public MetadataStatusResponse setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public String getOwnerProfile() {
        return ownerProfile;
    }

    public MetadataStatusResponse setOwnerProfile(String ownerProfile) {
        this.ownerProfile = ownerProfile;
        return this;
    }

    public String getAuthorName() {
        return authorName;
    }

    public MetadataStatusResponse setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public String getAuthorProfile() {
        return authorProfile;
    }

    public MetadataStatusResponse setAuthorProfile(String authorProfile) {
        this.authorProfile = authorProfile;
        return this;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getCurrentStatusDetails() {
        if(loadFull) {
            return metadataStatusObject.getCurrentState();
        } else {
            return "";
        }
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getPreviousStatusDetails() {
        if(loadFull) {
            return metadataStatusObject.getPreviousState();
        } else {
            return "";
        }
    }

    public MetadataStatusResponse setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    public MetadataStatusResponse setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
        return this;
    }
    public String getTitle() {
        return title;
    }

    public MetadataStatusResponse setTitle(String title) {
        this.title = title;
        return this;
    }

    public MetadataStatusId getId() {
        return metadataStatusObject.getId();
    }

    public String getChangeMessage() {
        return metadataStatusObject.getChangeMessage();
    }

    public Integer getOwner() {
        return metadataStatusObject.getOwner();
    }

    public ISODate getDueDate() {
        return metadataStatusObject.getDueDate();
    }

    public ISODate getCloseDate() {
        return metadataStatusObject.getCloseDate();
    }

    public StatusValue getStatusValue() {
        return metadataStatusObject.getStatusValue();
    }

}
