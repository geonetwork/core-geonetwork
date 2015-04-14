package org.fao.geonet.services.metadata.format.cache;

/**
 * A strategy for checking if a value in the cache is still valid (For example, if the change date is the same in the cache as
 * in the database or index).
 *
 * @author Jesse on 3/5/2015.
 */
public interface Validator {
    /**
     * Return true is the value stored in the cache is still valid.
     *
     * @param info the info from the cache, the result field will be null.
     */
    boolean isCacheVersionValid(StoreInfo info);
}
