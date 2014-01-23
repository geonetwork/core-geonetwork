package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.CswCapabilitiesInfoField;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * CswCapabilitiesInfoField: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class CswCapabilitiesInfoFieldEntityListenerManager extends AbstractEntityListenerManager<CswCapabilitiesInfoField> {
    @PrePersist
    public void prePresist(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final CswCapabilitiesInfoField entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
