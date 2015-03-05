package org.fao.geonet.services.metadata.format.cache;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Jesse on 3/5/2015.
 */
public class MemoryPersistentStore implements PersistentStore {
    Map<Key, StoreInfoAndData> dataMap = new HashMap<>();
    @Override
    public StoreInfoAndData get(Key key) {
        return dataMap.get(key);
    }

    @Override
    public StoreInfo getInfo(Key key) {
        return dataMap.get(key);
    }

    @Override
    public void put(Key key, StoreInfoAndData data) {
        this.dataMap.put(key, data);
    }

    @Nullable
    @Override
    public String getPublic(Key key) {
        final StoreInfoAndData storeInfoAndData = dataMap.get(key);
        if (storeInfoAndData.isPublished()) {
            return storeInfoAndData.getResult();
        }
        return null;
    }
}
