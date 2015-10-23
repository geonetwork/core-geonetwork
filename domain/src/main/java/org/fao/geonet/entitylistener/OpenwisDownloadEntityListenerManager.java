package org.fao.geonet.entitylistener;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.fao.geonet.domain.OpenwisDownload;

/**
 * Created with IntelliJ IDEA. UserGroup: Jesse Date: 11/26/13 Time: 1:51 PM
 */
public class OpenwisDownloadEntityListenerManager
        extends AbstractEntityListenerManager<OpenwisDownload> {
    @PrePersist
    public void prePresist(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PrePersist, entity);
    }

    @PreRemove
    public void preRemove(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PreRemove, entity);
    }

    @PostPersist
    public void postPersist(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PostPersist, entity);
    }

    @PostRemove
    public void postRemove(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PostRemove, entity);
    }

    @PreUpdate
    public void preUpdate(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PreUpdate, entity);
    }

    @PostUpdate
    public void postUpdate(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PostUpdate, entity);
    }

    @PostLoad
    public void postLoad(final OpenwisDownload entity) {
        handleEvent(PersistentEventType.PostLoad, entity);
    }
}
