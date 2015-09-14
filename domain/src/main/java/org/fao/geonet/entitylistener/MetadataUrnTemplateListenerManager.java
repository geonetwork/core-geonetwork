package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataUrnTemplate;

import javax.persistence.*;

/**
 * Created by jose on 11/09/15.
 */
public class MetadataUrnTemplateListenerManager extends AbstractEntityListenerManager<MetadataUrnTemplate> {
    @PrePersist
    public void prePresist(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataUrnTemplate entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
