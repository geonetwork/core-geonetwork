package org.fao.geonet.client.model;


public class EndpointStatus {
    public String url;
    public String urlGetRecords;
    public String state;
    public String createTimeUTC;
    public String lastUpdateUTC;

    public int expectedNumberOfRecords; //might be 0 if not processed yet
    public int numberOfRecordsReceived;

}
