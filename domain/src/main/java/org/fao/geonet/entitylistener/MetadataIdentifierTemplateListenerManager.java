package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataIdentifierTemplate;

import javax.persistence.*;

/**
 * @author Jose García
 */
public class MetadataIdentifierTemplateListenerManager extends AbstractEntityListenerManager<MetadataIdentifierTemplate> {
    @PrePersist
    public void prePresist(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataIdentifierTemplate entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
