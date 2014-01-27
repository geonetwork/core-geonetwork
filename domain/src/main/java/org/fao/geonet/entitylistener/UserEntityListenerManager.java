package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.User;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class UserEntityListenerManager extends AbstractEntityListenerManager<User> {
    @PrePersist
    public void prePresist(final User entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final User entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final User entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final User entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final User entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final User entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final User entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
