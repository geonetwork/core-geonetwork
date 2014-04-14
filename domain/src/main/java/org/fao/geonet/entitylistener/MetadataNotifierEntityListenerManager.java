package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataNotifier;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataNotifier: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataNotifierEntityListenerManager extends AbstractEntityListenerManager<MetadataNotifier> {
    @PrePersist
    public void prePresist(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataNotifier entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
