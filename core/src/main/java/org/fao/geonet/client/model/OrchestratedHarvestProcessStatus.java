package org.fao.geonet.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to model the remote harvester status.
 */
public class OrchestratedHarvestProcessStatus {
    String processID;
    OrchestratedHarvestProcessState orchestratedHarvestProcessState;

    HarvestStatus harvestStatus;
    LinkCheckStatus linkCheckStatus;
    IngestStatus ingestStatus;

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
}
