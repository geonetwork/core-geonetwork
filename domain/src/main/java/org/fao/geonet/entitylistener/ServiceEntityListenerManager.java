package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Service;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Service: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class ServiceEntityListenerManager extends AbstractEntityListenerManager<Service> {
    @PrePersist
    public void prePresist(final Service entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Service entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Service entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Service entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Service entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Service entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Service entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
