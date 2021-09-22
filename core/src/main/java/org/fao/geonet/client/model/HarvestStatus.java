package org.fao.geonet.client.model;


import java.util.List;

public class HarvestStatus {
    public String processID;
    public String url;
    public String longTermTag;
    public String state;
    public String createTimeUTC;
    public String lastUpdateUTC;
    public List<String> errorMessage;

    public List<List<String>> stackTraces;
    public List<EndpointStatus> endpoints;


}
