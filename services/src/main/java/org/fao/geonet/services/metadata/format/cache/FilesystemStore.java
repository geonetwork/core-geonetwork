package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * A {@link org.fao.geonet.services.metadata.format.cache.PersistentStore} that saves the files to disk.
 *
 * @author Jesse on 3/5/2015.
 */
public class FilesystemStore implements PersistentStore {
    @Autowired
    private GeonetworkDataDirectory dataDirectory;


    @Override
    public StoreInfoAndData get(Key key) {
        throw new UnsupportedOperationException("to implement");
    }

    @Override
    public StoreInfo getInfo(Key key) {
        throw new UnsupportedOperationException("to implement");
    }

    @Override
    public void put(Key key, StoreInfoAndData data) {
        throw new UnsupportedOperationException("to implement");
    }

    @Nullable
    @Override
    public String getPublic(Key key) {
        throw new UnsupportedOperationException("to implement");
    }
}
