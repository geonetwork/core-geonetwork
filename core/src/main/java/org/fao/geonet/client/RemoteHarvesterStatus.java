package org.fao.geonet.client;

import java.util.List;

/**
 * Class to model the remote harvester status.
 */
public class RemoteHarvesterStatus {
    String processID;
    String url;
    String longTermTag;
    String state;
    String createTimeUTC;
    String lastUpdateUTC;
    List<RemoteHarvesterStatusEndpoint> endpoints;

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public List<RemoteHarvesterStatusEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<RemoteHarvesterStatusEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    private class RemoteHarvesterStatusEndpoint {
        String url;
        String urlGetRecords;
        String state;
        String createTimeUTC;
        String lastUpdateUTC;
        int expectedNumberOfRecords;
        int numberOfRecordsReceived;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrlGetRecords() {
            return urlGetRecords;
        }

        public void setUrlGetRecords(String urlGetRecords) {
            this.urlGetRecords = urlGetRecords;
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

        public int getExpectedNumberOfRecords() {
            return expectedNumberOfRecords;
        }

        public void setExpectedNumberOfRecords(int expectedNumberOfRecords) {
            this.expectedNumberOfRecords = expectedNumberOfRecords;
        }

        public int getNumberOfRecordsReceived() {
            return numberOfRecordsReceived;
        }

        public void setNumberOfRecordsReceived(int numberOfRecordsReceived) {
            this.numberOfRecordsReceived = numberOfRecordsReceived;
        }
    }
}
