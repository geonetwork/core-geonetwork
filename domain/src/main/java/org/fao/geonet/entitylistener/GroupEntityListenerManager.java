package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Group;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Group: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class GroupEntityListenerManager extends AbstractEntityListenerManager<Group> {
    @PrePersist
    public void prePresist(final Group entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Group entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Group entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Group entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Group entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Group entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Group entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
