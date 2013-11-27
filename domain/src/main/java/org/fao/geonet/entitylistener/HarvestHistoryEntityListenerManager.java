package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.HarvestHistory;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * HarvestHistory: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class HarvestHistoryEntityListenerManager extends AbstractEntityListenerManager<HarvestHistory> {
    @PrePersist
    public void prePresist(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final HarvestHistory entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
