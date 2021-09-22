package org.fao.geonet.client.model;

public enum OrchestratedHarvestProcessState {
    CREATED,
    HAVESTING,
    LINKCHECKING,
    INGESTING,

    COMPLETE, USERABORT, ERROR
}
