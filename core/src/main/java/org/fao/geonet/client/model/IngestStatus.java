//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.client.model;

/**
 * Ingester process status model.
 */
public class IngestStatus {
    public String processID;
    public String longTermTag;
    public String state;
    public String createTimeUTC;
    public String lastUpdateUTC;
    public long totalRecords;
    public long numberOfRecordsIngested;
    public long numberOfRecordsIndexed;


    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getLongTermTag() {
        return longTermTag;
    }

    public void setLongTermTag(String longTermTag) {
        this.longTermTag = longTermTag;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreateTimeUTC() {
        return createTimeUTC;
    }

    public void setCreateTimeUTC(String createTimeUTC) {
        this.createTimeUTC = createTimeUTC;
    }

    public String getLastUpdateUTC() {
        return lastUpdateUTC;
    }

    public void setLastUpdateUTC(String lastUpdateUTC) {
        this.lastUpdateUTC = lastUpdateUTC;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getNumberOfRecordsIngested() {
        return numberOfRecordsIngested;
    }

    public void setNumberOfRecordsIngested(long numberOfRecordsIngested) {
        this.numberOfRecordsIngested = numberOfRecordsIngested;
    }

    public long getNumberOfRecordsIndexed() {
        return numberOfRecordsIndexed;
    }

    public void setNumberOfRecordsIndexed(long numberOfRecordsIndexed) {
        this.numberOfRecordsIndexed = numberOfRecordsIndexed;
    }

    @Override
    public String toString() {
        return "IngestStatus{" +
            "processID='" + processID + '\'' +
            ", longTermTag='" + longTermTag + '\'' +
            ", state='" + state + '\'' +
            ", createTimeUTC='" + createTimeUTC + '\'' +
            ", lastUpdateUTC='" + lastUpdateUTC + '\'' +
            ", totalRecords=" + totalRecords +
            ", numberOfRecordsIngested=" + numberOfRecordsIngested +
            ", numberOfRecordsIndexed=" + numberOfRecordsIndexed +
            '}';
    }
}
