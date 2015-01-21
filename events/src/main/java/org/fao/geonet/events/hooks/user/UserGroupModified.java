/**
 * 
 */
package org.fao.geonet.events.hooks.user;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.user.GroupJoined;
import org.fao.geonet.events.user.GroupLeft;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Hook events to database events
 * 
 * @author delawen
 * 
 */
@Component
public class UserGroupModified implements GeonetworkEntityListener<UserGroup>,
        ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     * @return
     */
    @Override
    public Class<UserGroup> getEntityClass() {
        return UserGroup.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     *      java.lang.Object)
     * @param arg0
     * @param arg1
     */
    @Override
    public void handleEvent(PersistentEventType type, UserGroup entity) {
        if (type == PersistentEventType.PrePersist
                || type == PersistentEventType.PreUpdate) {
            this.eventPublisher.publishEvent(new GroupJoined(entity));
        } else if (type == PersistentEventType.PostRemove) {
            this.eventPublisher.publishEvent(new GroupLeft(entity));
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
