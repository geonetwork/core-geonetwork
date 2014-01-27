package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataValidation;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataValidation: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataValidationEntityListenerManager extends AbstractEntityListenerManager<MetadataValidation> {
    @PrePersist
    public void prePresist(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataValidation entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
