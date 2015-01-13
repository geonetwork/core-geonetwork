/**
 * 
 */
package org.fao.geonet.events.hooks.md;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.md.MetadataAdd;
import org.fao.geonet.events.md.MetadataRemove;
import org.fao.geonet.events.md.MetadataUpdate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Hook events to database events
 * 
 * @author delawen
 * 
 * 
 */
@Component
public class MetadataModified implements GeonetworkEntityListener<Metadata>,
        ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     * @return
     */
    @Override
    public Class<Metadata> getEntityClass() {
        return Metadata.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     *      java.lang.Object)
     * @param arg0
     * @param arg1
     */
    @Override
    public void handleEvent(PersistentEventType type, Metadata entity) {
        if (entity.getId() == 0
                && (type == PersistentEventType.PrePersist || type == PersistentEventType.PreUpdate)) {
            this.eventPublisher.publishEvent(new MetadataAdd(entity));
        } else if ((type == PersistentEventType.PostPersist || type == PersistentEventType.PostUpdate)
                && entity.getId() != 0) {
            this.eventPublisher.publishEvent(new MetadataUpdate(entity));
        } else if (type == PersistentEventType.PostRemove) {
            this.eventPublisher.publishEvent(new MetadataRemove(entity));
        }
    }

    /**
     * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
     * @param applicationEventPublisher
     */
    @Override
    public void setApplicationEventPublisher(
            ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}
