package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.HarvesterData;
import org.fao.geonet.domain.HarvesterData;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * HarvesterData: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class HarvesterDataEntityListenerManager extends AbstractEntityListenerManager<HarvesterData> {
    @PrePersist
    public void prePresist(final HarvesterData entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final HarvesterData entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final HarvesterData entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final HarvesterData entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final HarvesterData entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final HarvesterData entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final HarvesterData entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
