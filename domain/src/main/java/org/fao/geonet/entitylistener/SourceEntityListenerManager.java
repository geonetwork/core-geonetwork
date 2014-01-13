package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Source;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Source: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class SourceEntityListenerManager extends AbstractEntityListenerManager<Source> {
    @PrePersist
    public void prePresist(final Source entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Source entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Source entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Source entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Source entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Source entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Source entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
