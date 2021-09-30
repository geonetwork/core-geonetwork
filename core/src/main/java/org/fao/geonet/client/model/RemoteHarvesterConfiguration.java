package org.fao.geonet.client.model;

import java.util.HashMap;
import java.util.Objects;

public class RemoteHarvesterConfiguration {
    /**
     * According to the CSW spec, when you request the last set of records (GetRecords), the response should have nextRecord=0.
     * Some servers return a number (i.e. if you request 100-111, it will return 112, even through there is no record 112).
     * ERROR - throw error if this occurs
     * IGNORE - do not throw
     */
    private static String KEY_LAST_RECORDSET_NEXTRECORD_NOT_ZERO = "LAST_RECORDSET_NEXTRECORD_NOT_ZERO";

    /**
     * Say you request records 100-111.  The response should have nextRecord=112.
     * If the server doesn't return 112, then this indicates a problem.
     * ERROR - throw error if this occurs
     * IGNORE - do not throw
     */
    private static String KEY_NEXTRECORD_BAD_VALUE = "NEXTRECORD_BAD_VALUE";

    /**
     * Say you request records 100-111 (i.e. startRecord=100, numberOfRecords=10).  The response should contain 10 records.
     * If the response contains fewer records than requested, this indicates an issue.
     * ERROR - throw error if this occurs
     * IGNORE - do not throw
     */
    private static String KEY_RESPONSE_CONTAINS_FEWER_RECORDS_THAN_REQUESTED = "RESPONSE_CONTAINS_FEWER_RECORDS_THAN_REQUESTED";

    /**
     * During a harvest, the server may add/delete records.  In this case, the GetRecord responses will have a different
     * totalNumberOfRecords than earlier (i.e. during DetermineWork).
     * <p>
     * ERROR - throw error if this occurs.  You will likely be missing records or have duplicate records.
     * IGNORE - (not recommended).  If the % amount of the number of records changed is greater than KEY_MAX_PERCENT_TOTAL_RECORDS_ALLOWED
     * then throw, otherwise do not.
     * For example, if total number of records=100 at the start of harvesting.  Later it becomes 105.  That's a 5% change.
     * If KEY_MAX_PERCENT_TOTAL_RECORDS_ALLOWED >=5 then its "ok" otherwise ERROR.
     */
    private static String KEY_TOTAL_RECORDS_CHANGED = "TOTAL_RECORDS_CHANGED";
    private static String KEY_MAX_PERCENT_TOTAL_RECORDS_ALLOWED = "MAX_PERCENT_TOTAL_RECORDS_ALLOWED";

    /**
     * After harvesting, look at the UUID for the records.  If there are duplicates, then something major has occurred.  The ingest will likely have issues with this.
     * ERROR - (recommended) throw error if this occurs
     * IGNORE - do not throw
     */
    private static String KEY_DUPLICATE_UUIDS = "DUPLICATE_UUIDS";


    private String url;
    private String longTermTag;
    //CSW <ogc:Filter>
    private String filter;
    private boolean lookForNestedDiscoveryService;

    private int numberOfRecordsPerRequest;

    HashMap<String, String> problematicResultsConfiguration = new HashMap<>();

    private String getRecordQueueHint;

    private boolean doNotSort;

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

    public boolean isLookForNestedDiscoveryService() {
        return lookForNestedDiscoveryService;
    }

    public void setLookForNestedDiscoveryService(boolean lookForNestedDiscoveryService) {
        this.lookForNestedDiscoveryService = lookForNestedDiscoveryService;
    }

    public int getNumberOfRecordsPerRequest() {
        return numberOfRecordsPerRequest;
    }

    public void setNumberOfRecordsPerRequest(int numberOfRecordsPerRequest) {
        this.numberOfRecordsPerRequest = numberOfRecordsPerRequest;
    }

    public HashMap<String, String> getProblematicResultsConfiguration() {
        return problematicResultsConfiguration;
    }

    public void setProblematicResultsConfiguration(HashMap<String, String> problematicResultsConfiguration) {
        this.problematicResultsConfiguration = problematicResultsConfiguration;
    }

    public boolean isDoNotSort() {
        return doNotSort;
    }

    public void setDoNotSort(boolean doNotSort) {
        this.doNotSort = doNotSort;
    }

    public void setErrorConfigNextRecordsNotZero(boolean errorConfigNextRecordsNotZero) {
        problematicResultsConfiguration.put(KEY_LAST_RECORDSET_NEXTRECORD_NOT_ZERO, errorConfigNextRecordsNotZero?"ERROR":"IGNORE");
    }

    public void setErrorConfigNextRecordsBadValue(boolean errorConfigNextRecordsBadValue) {
        problematicResultsConfiguration.put(KEY_NEXTRECORD_BAD_VALUE, errorConfigNextRecordsBadValue?"ERROR":"IGNORE");
    }

    public void setErrorConfigFewerRecordsThanRequested(boolean errorConfigFewerRecordsThanRequested) {
        problematicResultsConfiguration.put(KEY_RESPONSE_CONTAINS_FEWER_RECORDS_THAN_REQUESTED, errorConfigFewerRecordsThanRequested?"ERROR":"IGNORE");
    }

    public void setErrorConfigTotalRecordsChanged(boolean errorConfigTotalRecordsChanged) {
        problematicResultsConfiguration.put(KEY_TOTAL_RECORDS_CHANGED, errorConfigTotalRecordsChanged?"ERROR":"IGNORE");
    }

    public void setErrorConfigMaxPercentTotalRecordsChangedAllowed(int errorConfigMaxPercentTotalRecordsChangedAllowed) {
        problematicResultsConfiguration.put(KEY_MAX_PERCENT_TOTAL_RECORDS_ALLOWED, String.valueOf(errorConfigMaxPercentTotalRecordsChangedAllowed));
    }

    public void setErrorConfigDuplicatedUuids(boolean errorConfigDuplicatedUuids) {
        problematicResultsConfiguration.put(KEY_DUPLICATE_UUIDS, String.valueOf(errorConfigDuplicatedUuids));
    }

    public String getGetRecordQueueHint() {
        return getRecordQueueHint;
    }

    public void setGetRecordQueueHint(String getRecordQueueHint) {
        this.getRecordQueueHint = getRecordQueueHint;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteHarvesterConfiguration that = (RemoteHarvesterConfiguration) o;
        return lookForNestedDiscoveryService == that.lookForNestedDiscoveryService && numberOfRecordsPerRequest == that.numberOfRecordsPerRequest && doNotSort == that.doNotSort && url.equals(that.url) && Objects.equals(longTermTag, that.longTermTag) && Objects.equals(filter, that.filter) && Objects.equals(problematicResultsConfiguration, that.problematicResultsConfiguration) && Objects.equals(getRecordQueueHint, that.getRecordQueueHint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, longTermTag, filter, lookForNestedDiscoveryService, numberOfRecordsPerRequest, problematicResultsConfiguration, getRecordQueueHint, doNotSort);
    }
}
