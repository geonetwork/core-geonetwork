package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.HarvesterSetting;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * HarvesterSetting: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class HarvesterSettingEntityListenerManager extends AbstractEntityListenerManager<HarvesterSetting> {
    @PrePersist
    public void prePresist(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final HarvesterSetting entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
