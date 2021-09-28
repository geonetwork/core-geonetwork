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


import java.util.ArrayList;
import java.util.List;

/**
 * Link checker process status model.
 */
public class LinkCheckStatus {

    String processID;
    String linkCheckJobState;

    DocumentTypeStatus serviceRecordStatus;
    DocumentTypeStatus datasetRecordStatus;

    public List<String> errorMessage;
    public List<List<String>> stackTraces;

    public LinkCheckStatus() {
        errorMessage = new ArrayList<>();
        stackTraces = new ArrayList<>();
    }


    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getLinkCheckJobState() {
        return linkCheckJobState;
    }

    public void setLinkCheckJobState(String linkCheckJobState) {
        this.linkCheckJobState = linkCheckJobState;
    }

    public DocumentTypeStatus getServiceRecordStatus() {
        return serviceRecordStatus;
    }

    public void setServiceRecordStatus(DocumentTypeStatus serviceRecordStatus) {
        this.serviceRecordStatus = serviceRecordStatus;
    }

    public DocumentTypeStatus getDatasetRecordStatus() {
        return datasetRecordStatus;
    }

    public void setDatasetRecordStatus(DocumentTypeStatus datasetRecordStatus) {
        this.datasetRecordStatus = datasetRecordStatus;
    }

    @Override
    public String toString() {
        return "LinkCheckStatus{" +
            "processID='" + processID + '\'' +
            ", linkCheckJobState='" + linkCheckJobState + '\'' +
            ", serviceRecordStatus=" + serviceRecordStatus +
            ", datasetRecordStatus=" + datasetRecordStatus +
            ", errorMessage=" + errorMessage +
            ", stackTraces=" + stackTraces +
            '}';
    }
}
