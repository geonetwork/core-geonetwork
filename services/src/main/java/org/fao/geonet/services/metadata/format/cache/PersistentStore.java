package org.fao.geonet.services.metadata.format.cache;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
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
    StoreInfoAndData get(@Nonnull Key key) throws IOException, SQLException;

    /**
     * Get the stored value from the store;
     * @param key the key to use for retrieval.
     * @return return the value and associated data
     */
    StoreInfo getInfo(@Nonnull Key key) throws SQLException;

    /**
     * Put data in this store.
     *
     * @param key the cache key
     * @param data the data to cache
     */
    void put(@Nonnull Key key, @Nonnull StoreInfoAndData data) throws IOException, SQLException;

    /**
     * Return the cached value if it has been cached and is public, otherwise null.
     * @param key the lookup key.
     */
    @Nullable
    byte[] getPublished(@Nonnull Key key) throws IOException, SQLException;

    /**
     * Remove values with the key from the cache.
     *
     * @param key the lookup key
     */
    void remove(@Nonnull Key key) throws IOException, SQLException;

    /**
     * Publish or unpublish all cached values related to the given metadata.
     *
     * @param metadataId the id of the metadata whose published state may have changed
     * @param published  mark all cached values for this metadata
     */
    void setPublished(int metadataId, boolean published) throws IOException;
}
