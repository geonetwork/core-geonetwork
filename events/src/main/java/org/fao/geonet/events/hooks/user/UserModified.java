/**
 * 
 */
package org.fao.geonet.events.hooks.user;

import org.fao.geonet.domain.User;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.user.UserCreated;
import org.fao.geonet.events.user.UserDeleted;
import org.fao.geonet.events.user.UserUpdated;
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
public class UserModified implements GeonetworkEntityListener<User>,
        ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     * @return
     */
    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     *      java.lang.Object)
     * @param arg0
     * @param arg1
     */
    @Override
    public void handleEvent(PersistentEventType type, User entity) {
        if (entity.getId() == 0
                && (type == PersistentEventType.PrePersist || type == PersistentEventType.PreUpdate)) {
            this.eventPublisher.publishEvent(new UserCreated(entity));
        } else if ((type == PersistentEventType.PostPersist || type == PersistentEventType.PostUpdate)
                && entity.getId() != 0) {
            this.eventPublisher.publishEvent(new UserUpdated(entity));
        } else if (type == PersistentEventType.PostRemove) {
            this.eventPublisher.publishEvent(new UserDeleted(entity));
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
