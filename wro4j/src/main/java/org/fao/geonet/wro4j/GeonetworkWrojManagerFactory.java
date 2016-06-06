package org.fao.geonet.wro4j;

import java.util.Properties;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;

/**
 * User: Jesse Date: 11/25/13 Time: 8:35 AM
 */
public class GeonetworkWrojManagerFactory extends ConfigurableWroManagerFactory {
    public static final String WRO4J_LOG = "geonetwork.wro4j";
    private static final String CACHE_PROP_KEY = "cacheStrategy";
    private static final java.lang.String SIZE_PROP_KEY = "lruSize";

    private DiskbackedCache diskBackedCache = null;

    @Override
    protected WroModelFactory newModelFactory() {
        return new GeonetWroModelFactory() {
            @Override
            protected Properties getConfigProperties() {
                return newConfigProperties();
            }
        };
    }

    @Override
    protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
        Properties properties = newConfigProperties();
        int lruSize = Integer.parseInt(properties.getProperty(SIZE_PROP_KEY, "128"));
        switch (properties.getProperty(CACHE_PROP_KEY, "lru")) {
            case DiskbackedCache.NAME:
                if (diskBackedCache == null) {
                    diskBackedCache = new DiskbackedCache(lruSize, null);
                }
                return diskBackedCache;
            default:
                return new LruMemoryCacheStrategy<>(lruSize);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.diskBackedCache != null) {
            diskBackedCache.destroy();
        }
    }
}
