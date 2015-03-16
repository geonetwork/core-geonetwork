package org.fao.geonet.services.metadata.format.cache;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
