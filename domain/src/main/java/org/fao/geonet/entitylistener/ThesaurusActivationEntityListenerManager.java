package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.ThesaurusActivation;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * ThesaurusActivation: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class ThesaurusActivationEntityListenerManager extends AbstractEntityListenerManager<ThesaurusActivation> {
    @PrePersist
    public void prePresist(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final ThesaurusActivation entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
