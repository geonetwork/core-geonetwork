package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Address;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Address: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class AddressEntityListenerManager extends AbstractEntityListenerManager<Address> {
    @PrePersist
    public void prePresist(final Address entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Address entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Address entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Address entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Address entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Address entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Address entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
