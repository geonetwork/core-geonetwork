package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataCategory;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataCategory: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataCategoryEntityListenerManager extends AbstractEntityListenerManager<MetadataCategory> {
    @PrePersist
    public void prePresist(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataCategory entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
