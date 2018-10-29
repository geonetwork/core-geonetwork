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

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class MetadataWorkflowStatusResponse {
    private MetadataStatus currentStatus;
    private List<User> reviewers;
    private boolean hasEditPermission;
    private List<StatusValue> status;
    public MetadataWorkflowStatusResponse() {};

    public MetadataWorkflowStatusResponse(MetadataStatus currentStatus,
                                          List<User> reviewers,
                                          boolean hasEditPermission,
                                          List<StatusValue> status) {
        this.currentStatus = currentStatus;
        this.reviewers = reviewers;
        this.hasEditPermission = hasEditPermission;
        this.status = status;
    }

    public MetadataStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(MetadataStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<User> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
    }

    public boolean isHasEditPermission() {
        return hasEditPermission;
    }

    public void setHasEditPermission(boolean hasEditPermission) {
        this.hasEditPermission = hasEditPermission;
    }

    public List<StatusValue> getStatus() {
        return status;
    }

    public void setStatus(List<StatusValue> status) {
        this.status = status;
    }
}
