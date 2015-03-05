package org.fao.geonet.services.metadata.format.cache;

/**
 * Encapsulates the information when a formatter is executed.
 * @author Jesse on 3/5/2015.
 */
public class StoreInfo {
    public final String result;
    public final long changeDate;
    public final boolean published;

    public StoreInfo(String result, long changeDate, boolean published) {
        this.result = result;
        this.changeDate = changeDate;
        this.published = published;
    }
}
