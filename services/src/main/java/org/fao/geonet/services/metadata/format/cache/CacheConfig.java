package org.fao.geonet.services.metadata.format.cache;

/**
 * Controls which requests should be cached by the {@link org.fao.geonet.services.metadata.format.cache.FormatterCache}.
 *
 * For example which formatters to cache, what types (only html and xml).
 *
 * @author Jesse on 3/6/2015.
 */
public interface CacheConfig {
    boolean allowCaching(Key key);
}
