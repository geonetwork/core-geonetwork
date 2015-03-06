package org.fao.geonet.services.metadata.format.cache;

/**
 * The metadata about the stored value.  Does not contain the value, only the data describing it like change data and published.
 *
 * @author Jesse on 3/5/2015.
 */
public class StoreInfo {
    private final long changeDate;
    private final boolean published;

    public StoreInfo(long changeDate, boolean published) {
        this.changeDate = changeDate;
        this.published = published;
    }

    public long getChangeDate() {
        return changeDate;
    }

    public boolean isPublished() {
        return published;
    }
}
