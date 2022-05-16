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

public class MetadataStatusResponse extends MetadataStatus {


    private String authorName;
    private String authorEmail;
    private String authorProfile;

    private String ownerName;
    private String ownerEmail;
    private String ownerProfile;

    private String dateDue;
    private String dateClose;
    private String dateChange;

    private String title;
    private String currentStatus;
    private String previousStatus;

    private boolean loadFull = false;

    public MetadataStatusResponse(MetadataStatus metadataStatus) {
        setUuid(metadataStatus.getUuid());
        setId(metadataStatus.getId());
        setUserId(metadataStatus.getUserId());
        setChangeDate(metadataStatus.getChangeDate());
        setChangeMessage(metadataStatus.getChangeMessage());
        setOwner(metadataStatus.getOwner());
        setDueDate(metadataStatus.getDueDate());
        setCloseDate(metadataStatus.getCloseDate());
        setStatusValue(metadataStatus.getStatusValue());
        setTitles(metadataStatus.getTitles());
        setRelatedMetadataStatus(metadataStatus.getRelatedMetadataStatus());
        setMetadataId(metadataStatus.getMetadataId());
        setCurrentState(metadataStatus.getCurrentState());
        setPreviousState(metadataStatus.getPreviousState());
    }

    public MetadataStatusResponse(MetadataStatus metadataStatus, boolean loadFull) {
        this(metadataStatus);
        this.loadFull = loadFull;
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

    public boolean isCurrentStateEmpty() {
        if (super.getCurrentState() == null || super.getCurrentState().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPreviousStateEmpty() {
        if (super.getPreviousState() == null || super.getPreviousState().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getCurrentState() {
        if(loadFull) {
            return super.getCurrentState();
        } else {
            return "";
        }
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    @Override
    public String getPreviousState() {
        if(loadFull) {
            return super.getPreviousState();
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

    public int getStatusId() {
        return getStatusValue().getId();
    }

    public void setDateDue(String dateDue) {
        this.dateDue = dateDue;
    }

    public String getDateDue() {
        return dateDue;
    }

    public String getDateClose() {
        return dateClose;
    }

    public void setDateClose(String dateClose) {
        this.dateClose = dateClose;
    }

    public String getDateChange() {
        return dateChange;
    }

    public void setDateChange(String dateChange) {
        this.dateChange = dateChange;
    }
}
