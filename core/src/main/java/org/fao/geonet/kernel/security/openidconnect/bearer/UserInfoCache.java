package org.fao.geonet.kernel.security.openidconnect.bearer;

import java.util.HashMap;
import java.util.Map;

/**
 * cache of UserInfoCacheItems (access token -> UserInfoCacheItem).
 * NOTE: if not found, null is returned.
 * NOTE: if the access token is expired, null is returned (and its removed from the cache)
 */
public class UserInfoCache {

    static Object lockobj = new Object();
    Map<String, UserInfoCacheItem> cache = new HashMap<>();

    public UserInfoCacheItem getItem(String accessKey) {
        synchronized (lockobj) {
            if (!cache.containsKey(accessKey))
                return null;
            UserInfoCacheItem item = cache.get(accessKey);
            if (item.isExpired()) {
                cache.remove(accessKey);
                return null;
            }
            return item;
        }
    }

    public void putItem(UserInfoCacheItem item) {
        synchronized (lockobj) {
            cache.put(item.getAccessToken(), item);
        }
    }
}
