/**
 * 
 */
package org.fao.geonet.events.hooks.group;

import org.fao.geonet.domain.Group;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.group.GroupCreated;
import org.fao.geonet.events.group.GroupRemoved;
import org.fao.geonet.events.group.GroupUpdated;
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
public class GroupModified implements GeonetworkEntityListener<Group>,
        ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     * @return
     */
    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     *      java.lang.Object)
     * @param arg0
     * @param arg1
     */
    @Override
    public void handleEvent(PersistentEventType type, Group entity) {
        if (entity.getId() == 0
                && (type == PersistentEventType.PrePersist || type == PersistentEventType.PreUpdate)) {
            this.eventPublisher.publishEvent(new GroupCreated(entity));
        } else if ((type == PersistentEventType.PostPersist || type == PersistentEventType.PostUpdate)
                && entity.getId() != 0) {
            this.eventPublisher.publishEvent(new GroupUpdated(entity));
        } else if (type == PersistentEventType.PostRemove) {
            this.eventPublisher.publishEvent(new GroupRemoved(entity));
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
