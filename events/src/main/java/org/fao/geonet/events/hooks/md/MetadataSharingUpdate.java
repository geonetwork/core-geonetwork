/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

/**
 *
 */
package org.fao.geonet.events.hooks.md;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.md.sharing.MetadataShare;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Hook events to database events
 *
 * @author delawen
 */
@Component
public class MetadataSharingUpdate implements GeonetworkEntityListener<OperationAllowed>,
    ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     */
    @Override
    public Class<OperationAllowed> getEntityClass() {
        return OperationAllowed.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     * java.lang.Object)
     */
    @Override
    public void handleEvent(PersistentEventType type, OperationAllowed entity) {
        if (type == PersistentEventType.PostPersist) {
            this.eventPublisher.publishEvent(new MetadataShare(entity, MetadataShare.Type.ADD));
        } else if (type == PersistentEventType.PostUpdate) {
            this.eventPublisher.publishEvent(new MetadataShare(entity, MetadataShare.Type.UPDATE));
        } else if (type == PersistentEventType.PostRemove) {
            this.eventPublisher.publishEvent(new MetadataShare(entity, MetadataShare.Type.REMOVE));
        }
    }

    /**
     * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
     */
    @Override
    public void setApplicationEventPublisher(
        ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}
