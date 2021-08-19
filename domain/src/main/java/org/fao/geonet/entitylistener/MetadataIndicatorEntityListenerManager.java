package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataIndicator;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class MetadataIndicatorEntityListenerManager extends AbstractEntityListenerManager<MetadataIndicator> {
    @PrePersist
    public void prePresist(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }

    @PreRemove
    public void preRemove(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }

    @PostPersist
    public void postPersist(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }

    @PostRemove
    public void postRemove(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }

    @PreUpdate
    public void preUpdate(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }

    @PostUpdate
    public void postUpdate(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }

    @PostLoad
    public void postLoad(final MetadataIndicator entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
