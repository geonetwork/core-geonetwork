package org.fao.geonet.services.metadata.format.cache;

/**
 * Encapsulates the information when a formatter is executed.
 * @author Jesse on 3/5/2015.
 */
public class StoreInfoAndData extends StoreInfo {
    private final String result;

    public StoreInfoAndData(String result, long changeDate, boolean published, long lastAccess, int popularity) {
        super(changeDate, published, popularity, lastAccess);
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
