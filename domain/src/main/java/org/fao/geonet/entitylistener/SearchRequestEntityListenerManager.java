package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.statistic.SearchRequest;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * SearchRequest: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class SearchRequestEntityListenerManager extends AbstractEntityListenerManager<SearchRequest> {
    @PrePersist
    public void prePresist(final SearchRequest entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final SearchRequest entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final SearchRequest entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final SearchRequest entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final SearchRequest entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final SearchRequest entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final SearchRequest entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
