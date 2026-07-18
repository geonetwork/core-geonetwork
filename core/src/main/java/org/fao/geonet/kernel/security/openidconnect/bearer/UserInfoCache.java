package org.fao.geonet.kernel.security.openidconnect.bearer;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache for storing UserInfoCacheItems, where the key is an access token and the value is a UserInfoCacheItem.
 */
public class UserInfoCache {

    /**
     * A lock object used to synchronize access to the cache.
     */
    static final Object lockobj = new Object();

    /**
     * The internal cache that maps access tokens to UserInfoCacheItems.
     */
    Map<String, UserInfoCacheItem> cache = new HashMap<>();

    /**
     * Retrieves a UserInfoCacheItem from the cache based on the provided access token.
     * Removes expired items from the cache before attempting to retrieve the item.
     *
     * @param accessKey The access token used as the key to retrieve the item.
     * @return The UserInfoCacheItem associated with the access token, or null if not found or expired.
     */
    public UserInfoCacheItem getItem(String accessKey) {
        synchronized (lockobj) {
            cache.entrySet().removeIf(e -> e.getValue().isExpired());
            return cache.get(accessKey);
        }
    }

    /**
     * Adds a UserInfoCacheItem to the cache.
     *
     * @param item The UserInfoCacheItem to be added to the cache.
     */
    public void putItem(UserInfoCacheItem item) {
        synchronized (lockobj) {
            cache.put(item.getRawToken(), item);
        }
    }
}
