package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Has the standard functionality for caching control.
 *
 * @author Jesse on 3/7/2015.
 */
public abstract class AbstractCacheConfig implements CacheConfig {
    @Autowired
    private SystemInfo systemInfo;

    @Override
    public final boolean allowCaching(Key key) {
        final boolean isTesting = systemInfo == null;
        return (isTesting || !systemInfo.isDevMode()) && extraChecks(key);
    }

    /**
     * Perform extra checks to allow caching.  Return false to disallow caching
     */
    protected abstract boolean extraChecks(Key key);
}
