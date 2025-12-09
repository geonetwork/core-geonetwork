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

import com.google.common.collect.Lists;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author Jesse on 3/5/2015.
 */
public class MemoryPersistentStore implements PersistentStore {
    Map<Key, StoreInfoAndData> dataMap = new HashMap<>();

    @Override
    public StoreInfoAndData get(@Nonnull Key key) {
        return dataMap.get(key);
    }

    @Override
    public StoreInfo getInfo(@Nonnull Key key) {
        return dataMap.get(key);
    }

    @Override
    public void put(@Nonnull Key key, @Nonnull StoreInfoAndData data) {
        this.dataMap.put(key, data);
    }

    @Nullable
    @Override
    public byte[] getPublished(@Nonnull Key key) {
        final StoreInfoAndData storeInfoAndData = dataMap.get(key);
        if (storeInfoAndData != null && storeInfoAndData.isPublished() && key.hideWithheld) {
            return storeInfoAndData.data;
        }
        return null;
    }

    @Override
    public void remove(@Nonnull Key key) throws IOException, SQLException {
        this.dataMap.remove(key);
    }

    @Override
    public void setPublished(int metadataId, boolean published) {
        for (Map.Entry<Key, StoreInfoAndData> dataEntry : Lists.newArrayList(dataMap.entrySet())) {
            final byte[] data = dataEntry.getValue().data;
            final long changeDate = dataEntry.getValue().getChangeDate();
            dataMap.put(dataEntry.getKey(), new StoreInfoAndData(data, changeDate, true));
        }
    }

    @Override
    public void clear() {
        this.dataMap.clear();
    }
}
