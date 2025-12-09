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

package org.fao.geonet.api.records.formatters.cache;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The strategy used by {@link PersistentStore} for storing each record in a persistent fashion.
 *
 * @author Jesse on 3/5/2015.
 */
public interface PersistentStore {

    /**
     * Get the stored value from the store;
     *
     * @param key the key to use for retrieval.
     * @return return the value and associated data
     */
    StoreInfoAndData get(@Nonnull Key key) throws IOException, SQLException;

    /**
     * Get the stored value from the store;
     *
     * @param key the key to use for retrieval.
     * @return return the value and associated data
     */
    StoreInfo getInfo(@Nonnull Key key) throws SQLException;

    /**
     * Put data in this store.
     *
     * @param key  the cache key
     * @param data the data to cache
     */
    void put(@Nonnull Key key, @Nonnull StoreInfoAndData data) throws IOException, SQLException;

    /**
     * Return the cached value if it has been cached and is public, otherwise null.
     *
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

    /**
     * Remove all cached elements from the cache.
     */
    void clear() throws SQLException, IOException;
}
