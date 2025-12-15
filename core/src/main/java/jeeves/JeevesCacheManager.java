/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package jeeves;

import org.fao.geonet.JeevesJCS;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JeevesCacheManager {

    /**
     * Cache region to store
     */
    private static final String TEN_SECOND_KEY = "JeevesCacheManagerTenSeconds";
    private static final String ETERNAL_KEY = "JeevesCacheManagerETERNAL";

    private static Map<String, Lock> locks = new HashMap<String, Lock>();

    /**
     * Looks in a very short term cache for the item if it is no longer in cache then loads the
     * value from the loader and caches the result in the cache.
     *
     * This is very useful for data that has to be very frequently accessed but perhaps a small
     * delay in updates is acceptable.
     *
     * For example translations of groups from the database are likely acceptable to be eventually
     * consistent.
     *
     * @param key    the key to use for looking up the object
     * @param loader an object for loading the data if the data has expired from the cache already
     */
    public static <V> V findInTenSecondCache(String key, Callable<V> loader) throws Exception {
        return find(TEN_SECOND_KEY, key, loader);
    }

    public static <V> V findInEternalCache(String key, Callable<V> loader) throws Exception {
        return find(ETERNAL_KEY, key, loader);
    }

    private static <V> V find(String cacheName, String key, Callable<V> loader) throws Exception {
        Lock lock = getLock(key);
        try {
            if (!lock.tryLock(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out trying to get the log for the JeevesCache");
            }
            JeevesJCS cache = JeevesJCS.getInstance(cacheName);

            @SuppressWarnings("unchecked")
            V value = (V) cache.get(key);

            if (value == null) {
                value = loader.call();
                if (value != null) {
                    cache.put(key, value);
                }
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    private static synchronized Lock getLock(String key) {
        Lock lock = locks.get(key);
        if (lock == null) {
            lock = new ReentrantLock();
            locks.put(key, lock);
        }
        return lock;
    }
}
