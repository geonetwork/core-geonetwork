package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.OperationAllowed;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * OperationAllowed: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class OperationAllowedEntityListenerManager extends AbstractEntityListenerManager<OperationAllowed> {
    @PrePersist
    public void prePresist(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final OperationAllowed entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
