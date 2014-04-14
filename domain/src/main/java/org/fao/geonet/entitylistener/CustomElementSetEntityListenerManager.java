package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.CustomElementSet;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * CustomElementSet: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class CustomElementSetEntityListenerManager extends AbstractEntityListenerManager<CustomElementSet> {
    @PrePersist
    public void prePresist(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final CustomElementSet entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
