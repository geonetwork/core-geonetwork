package org.fao.geonet.kernel.security.openidconnect.bearer;

import java.util.HashMap;
import java.util.Map;

public class UserInfoCache {

     Map<String,UserInfoCacheItem> cache = new HashMap<>();

     static Object lockobj = new Object();

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
            cache.put(item.getAccessToken(),item);
        }
    }
}
