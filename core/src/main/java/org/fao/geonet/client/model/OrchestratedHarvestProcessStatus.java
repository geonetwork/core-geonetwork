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
 * Remote harvester status.
 */
public class OrchestratedHarvestProcessStatus {
    String processID;
    OrchestratedHarvestProcessState orchestratedHarvestProcessState;

    HarvestStatus harvestStatus;
    LinkCheckStatus linkCheckStatus;
    IngestStatus ingestStatus;CswHarvester2

    public List<String> errorMessage;
    public List<List<String>> stackTraces;

    public OrchestratedHarvestProcessStatus(String processID, OrchestratedHarvestProcessState orchestratedHarvestProcessState) {
        this.processID = processID;
        this.orchestratedHarvestProcessState = orchestratedHarvestProcessState;

        errorMessage = new ArrayList<>();
        stackTraces = new ArrayList<>();
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public OrchestratedHarvestProcessState getOrchestratedHarvestProcessState() {
        return orchestratedHarvestProcessState;
    }

    public void setOrchestratedHarvestProcessState(OrchestratedHarvestProcessState orchestratedHarvestProcessState) {
        this.orchestratedHarvestProcessState = orchestratedHarvestProcessState;
    }

    public HarvestStatus getHarvestStatus() {
        return harvestStatus;
    }

    public void setHarvestStatus(HarvestStatus harvestStatus) {
        this.harvestStatus = harvestStatus;
    }

    public LinkCheckStatus getLinkCheckStatus() {
        return linkCheckStatus;
    }

    public void setLinkCheckStatus(LinkCheckStatus linkCheckStatus) {
        this.linkCheckStatus = linkCheckStatus;
    }

    public IngestStatus getIngestStatus() {
        return ingestStatus;
    }

    public void setIngestStatus(IngestStatus ingestStatus) {
        this.ingestStatus = ingestStatus;
    }


    public boolean isFinished() {
        return getOrchestratedHarvestProcessState().equals(OrchestratedHarvestProcessState.COMPLETE) ||
            getOrchestratedHarvestProcessState().equals(OrchestratedHarvestProcessState.ERROR) ||
            getOrchestratedHarvestProcessState().equals(OrchestratedHarvestProcessState.ERROR);
    }

    @Override
    public String toString() {
        return "OrchestratedHarvestProcessStatus{" +
            "processID='" + processID + '\'' +
            ", orchestratedHarvestProcessState=" + orchestratedHarvestProcessState +
            ", harvestStatus=" + harvestStatus +
            ", linkCheckStatus=" + linkCheckStatus +
            ", ingestStatus=" + ingestStatus +
            ", errorMessage=" + errorMessage +
            ", stackTraces=" + stackTraces +
            '}';
    }
}
