package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.IsoLanguage;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * IsoLanguage: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class IsoLanguageEntityListenerManager extends AbstractEntityListenerManager<IsoLanguage> {
    @PrePersist
    public void prePresist(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final IsoLanguage entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
