package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.events.md.MetadataIndexCompleted;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * This class is responsible for listening for metadata index events and updating the cache's publication values so that it stays in
 * sync with the actual metadata.
 *
 * @author Jesse on 3/6/2015.
 */
public class FormatterCachePublishListener implements ApplicationListener<MetadataIndexCompleted> {
    @Autowired
    private FormatterCache formatterCache;

    @Override
    public synchronized void onApplicationEvent(MetadataIndexCompleted event) {
        final int metadataId = event.getMd().getId();
        final Specification<OperationAllowed> isPublished = OperationAllowedSpecs.isPublic(ReservedOperation.view);
        final Specification<OperationAllowed> hasMdId = OperationAllowedSpecs.hasMetadataId(metadataId);
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        final OperationAllowed one = context.getBean(OperationAllowedRepository.class).findOne(where(hasMdId).and(isPublished));
        try {
            formatterCache.setPublished(metadataId, one != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
