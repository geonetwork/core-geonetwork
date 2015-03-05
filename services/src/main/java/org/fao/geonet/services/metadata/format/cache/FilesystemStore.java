package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link org.fao.geonet.services.metadata.format.cache.PersistentStore} that saves the files to disk.
 *
 * @author Jesse on 3/5/2015.
 */
public class FilesystemStore implements PersistentStore {
    @Autowired
    private GeonetworkDataDirectory dataDirectory;


}
