package org.fao.geonet.services.metadata.format.cache;

/**
 * Always forces the formatter to be loaded.
 *
 * @author Jesse on 3/6/2015.
 */
public class NoCacheValidator implements Validator {
    @Override
    public boolean isCacheVersionValid(StoreInfo info) {
        return false;
    }
}
