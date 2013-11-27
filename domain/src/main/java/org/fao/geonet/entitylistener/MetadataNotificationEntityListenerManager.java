package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataNotification;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataNotification: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataNotificationEntityListenerManager extends AbstractEntityListenerManager<MetadataNotification> {
    @PrePersist
    public void prePresist(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataNotification entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
