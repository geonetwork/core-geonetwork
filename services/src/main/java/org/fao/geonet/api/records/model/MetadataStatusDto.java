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

import java.util.LinkedHashMap;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/* Contains all the important fields from
      org.fao.geonet.domain.MetadataStatus
   and used as a model object for api mapping  */

public class MetadataStatusDto {

    private int id;
    private ISODate _changedate;
    private int metadataId;
    private int userId;
    private String changeMessage;
    private StatusValue statusValue;
    private int _owner;
    private ISODate _duedate;
    private ISODate _closedate;
    private String previousState;
    private String currentState;

    private MetadataStatus relatedMetadataStatus;
    private String uuid;
    private LinkedHashMap<String, String> titles;

    public MetadataStatusDto(MetadataStatus metadataStatus) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ISODate getChangeDate() {
        return _changedate;
    }

    public void setChangeDate(ISODate _changedate) {
        this._changedate = _changedate;
    }

    public int getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(int metadataId) {
        this.metadataId = metadataId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getChangeMessage() {
        return changeMessage;
    }

    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }

    public StatusValue getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(StatusValue statusValue) {
        this.statusValue = statusValue;
    }

    public Integer getOwner() {
        return _owner;
    }

    public void setOwner(int _owner) {
        this._owner = _owner;
    }

    public ISODate getDueDate() {
        return _duedate;
    }

    public void setDueDate(ISODate _duedate) {
        this._duedate = _duedate;
    }

    public ISODate getCloseDate() {
        return _closedate;
    }

    public void setCloseDate(ISODate _closedate) {
        this._closedate = _closedate;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getCurrentState() {
        return currentState;
    }


    @JsonProperty(value = "relatedMetadataStatusId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public MetadataStatus getRelatedMetadataStatus() {
        return relatedMetadataStatus;
    }
    public void setRelatedMetadataStatus(MetadataStatus relatedMetadataStatus) {
        this.relatedMetadataStatus = relatedMetadataStatus;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LinkedHashMap<String, String> getTitles() {
        return titles;
    }

    public void setTitles(LinkedHashMap<String, String> titles) {
        this.titles = titles;
    }
}
