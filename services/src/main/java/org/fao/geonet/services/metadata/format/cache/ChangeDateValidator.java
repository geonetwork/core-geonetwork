package org.fao.geonet.services.metadata.format.cache;

/**
 * Checks if the cache is up-to-date based on the changeDate.
 *
 * @author Jesse on 3/5/2015.
 */
public class ChangeDateValidator implements Validator {
    private final long changeDate;

    /**
     * The latest change date of the metadata.
     *
     * @param changeDate The latest change date of the metadata.
     */
    public ChangeDateValidator(long changeDate) {
        this.changeDate = changeDate;
    }

    @Override
    public boolean isCacheVersionValid(StoreInfo info) {
        return Math.abs(changeDate - info.getChangeDate()) < 500;
    }
}
