/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.MetadataStatus;

public class MetadataStatusResponse extends MetadataStatusDto {

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
        super(metadataStatus);
    }

    public MetadataStatusResponse(MetadataStatus metadataStatus, boolean loadFull) {
        this(metadataStatus);
        this.loadFull = loadFull;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerProfile() {
        return ownerProfile;
    }

    public void setOwnerProfile(String ownerProfile) {
        this.ownerProfile = ownerProfile;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorProfile() {
        return authorProfile;
    }

    public void setAuthorProfile(String authorProfile) {
        this.authorProfile = authorProfile;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public boolean isCurrentStateEmpty() {
        return super.getCurrentState() == null || super.getCurrentState().isEmpty();
    }

    public boolean isPreviousStateEmpty() {
        return super.getPreviousState() == null || super.getPreviousState().isEmpty();
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

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
