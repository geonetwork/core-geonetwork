package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataRelation;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataRelation: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataRelationEntityListenerManager extends AbstractEntityListenerManager<MetadataRelation> {
    @PrePersist
    public void prePresist(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataRelation entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
