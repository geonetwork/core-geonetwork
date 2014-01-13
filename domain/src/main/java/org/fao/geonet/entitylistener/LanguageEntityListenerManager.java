package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.Language;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * Language: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class LanguageEntityListenerManager extends AbstractEntityListenerManager<Language> {
    @PrePersist
    public void prePresist(final Language entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final Language entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final Language entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final Language entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final Language entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final Language entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final Language entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
