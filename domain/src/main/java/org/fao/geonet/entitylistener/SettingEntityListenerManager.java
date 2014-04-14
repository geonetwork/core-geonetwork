package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Setting;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Setting: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class SettingEntityListenerManager extends AbstractEntityListenerManager<Setting> {
    @PrePersist
    public void prePresist(final Setting entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Setting entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Setting entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Setting entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Setting entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Setting entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Setting entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
