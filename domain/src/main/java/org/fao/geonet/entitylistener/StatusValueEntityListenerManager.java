package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.StatusValue;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * StatusValue: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class StatusValueEntityListenerManager extends AbstractEntityListenerManager<StatusValue> {
    @PrePersist
    public void prePresist(final StatusValue entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final StatusValue entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final StatusValue entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final StatusValue entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final StatusValue entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final StatusValue entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final StatusValue entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
