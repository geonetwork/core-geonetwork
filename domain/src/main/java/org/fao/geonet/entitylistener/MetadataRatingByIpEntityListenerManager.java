package org.fao.geonet.entitylistener;

import org.fao.geonet.domain.MetadataRatingByIp;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * MetadataRatingByIp: Jesse
 * Date: 11/26/13
 * Time: 1:51 PM
 */
public class MetadataRatingByIpEntityListenerManager extends AbstractEntityListenerManager<MetadataRatingByIp> {
    @PrePersist
    public void prePresist(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }
    @PreRemove
    public void preRemove(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }
    @PostPersist
    public void postPersist(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }
    @PostRemove
    public void postRemove(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }
    @PreUpdate
    public void preUpdate(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }
    @PostUpdate
    public void postUpdate(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }
    @PostLoad
    public void postLoad(final MetadataRatingByIp entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
