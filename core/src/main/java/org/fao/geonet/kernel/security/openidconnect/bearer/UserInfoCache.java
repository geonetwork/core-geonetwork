package org.fao.geonet.kernel.security.openidconnect.bearer;

import java.util.HashMap;
import java.util.Map;

/**
 * cache of UserInfoCacheItems (access token -> UserInfoCacheItem).
 * NOTE: if not found, null is returned.
 * NOTE: if the access token is expired, null is returned (and its removed from the cache)
 */
public class UserInfoCache {

    static final Object lockobj = new Object();
    Map<String, UserInfoCacheItem> cache = new HashMap<>();

    public UserInfoCacheItem getItem(String accessKey) {
        synchronized (lockobj) {
            cache.entrySet().removeIf(e -> e.getValue().isExpired());
            return cache.get(accessKey);
        }
    }

    public void putItem(UserInfoCacheItem item) {
        synchronized (lockobj) {
            cache.put(item.getAccessToken(), item);
        }
    }
}
