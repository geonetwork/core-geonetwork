package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.UserGroup;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * UserGroup: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class UserGroupEntityListenerManager extends AbstractEntityListenerManager<UserGroup> {
    @PrePersist
    public void prePresist(final UserGroup entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final UserGroup entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final UserGroup entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final UserGroup entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final UserGroup entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final UserGroup entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final UserGroup entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
