package org.fao.geonet.entitylistener;


import org.fao.geonet.domain.statistic.SearchRequestParam;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * SearchRequestParam: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class SearchRequestParamEntityListenerManager extends AbstractEntityListenerManager<SearchRequestParam> {
    @PrePersist
    public void prePresist(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final SearchRequestParam entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
