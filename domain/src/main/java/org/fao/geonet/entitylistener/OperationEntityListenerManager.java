package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Operation;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Operation: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class OperationEntityListenerManager extends AbstractEntityListenerManager<Operation> {
    @PrePersist
    public void prePresist(final Operation entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Operation entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Operation entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Operation entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Operation entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Operation entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Operation entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
