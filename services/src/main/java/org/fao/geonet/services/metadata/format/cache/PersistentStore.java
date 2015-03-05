package org.fao.geonet.services.metadata.format.cache;

import javax.annotation.Nullable;

/**
 * The strategy used by {@link PersistentStore} for storing each record in a persistent
 * fashion.
 *
* @author Jesse on 3/5/2015.
*/
public interface PersistentStore {

    /**
     * Get the stored value from the store;
     * @param key the key to use for retrieval.
     * @return return the value and associated data
     */
    StoreInfoAndData get(Key key);

    /**
     * Get the stored value from the store;
     * @param key the key to use for retrieval.
     * @return return the value and associated data
     */
    StoreInfo getInfo(Key key);

    /**
     * Put data in this store.
     *
     * @param key the cache key
     * @param data the data to cache
     */
    void put(Key key, StoreInfoAndData data);

    /**
     * Return the cached value if it has been cached and is public, otherwise null.
     * @param key the lookup key.
     */
    @Nullable
    String getPublic(Key key);
}
