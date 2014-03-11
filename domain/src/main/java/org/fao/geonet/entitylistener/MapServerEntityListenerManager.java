package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MapServer;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MapServer: Francois
 */
public class MapServerEntityListenerManager
        extends AbstractEntityListenerManager<MapServer> {
    @PrePersist
    public void prePresist(final MapServer entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MapServer entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MapServer entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MapServer entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MapServer entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MapServer entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MapServer entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
