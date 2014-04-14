package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Metadata;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Metadata: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataEntityListenerManager extends AbstractEntityListenerManager<Metadata> {
    @PrePersist
    public void prePresist(final Metadata entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Metadata entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Metadata entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Metadata entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Metadata entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Metadata entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Metadata entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
