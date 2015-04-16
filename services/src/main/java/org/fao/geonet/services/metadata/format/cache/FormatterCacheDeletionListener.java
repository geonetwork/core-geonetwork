package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.events.md.MetadataRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class is responsible for listening for metadata index events and updating the cache's publication values so that it stays in
 * sync with the actual metadata.
 *
 * @author Jesse on 3/6/2015.
 */
public class FormatterCacheDeletionListener implements ApplicationListener<MetadataRemove> {
    @Autowired
    private FormatterCache formatterCache;

    @Override
    public synchronized void onApplicationEvent(MetadataRemove event) {
        final int metadataId = event.getMd().getId();
        try {
            formatterCache.removeAll(metadataId);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
