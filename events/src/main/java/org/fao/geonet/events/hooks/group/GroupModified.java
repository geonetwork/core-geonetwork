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
package org.fao.geonet.events.hooks.group;

import org.fao.geonet.domain.Group;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.fao.geonet.events.group.GroupCreated;
import org.fao.geonet.events.group.GroupRemoved;
import org.fao.geonet.events.group.GroupUpdated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Hook events to database events
 *
 * @author delawen
 */
@Component
public class GroupModified implements GeonetworkEntityListener<Group>,
    ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#getEntityClass()
     */
    @Override
    public Class<Group> getEntityClass() {
        return Group.class;
    }

    /**
     * @see org.fao.geonet.entitylistener.GeonetworkEntityListener#handleEvent(org.fao.geonet.entitylistener.PersistentEventType,
     * java.lang.Object)
     */
    @Override
    public void handleEvent(PersistentEventType type, Group entity) {
        if (type == PersistentEventType.PrePersist) {
            this.eventPublisher.publishEvent(new GroupCreated(entity));
        } else if (type == PersistentEventType.PreUpdate) {
            this.eventPublisher.publishEvent(new GroupUpdated(entity));
        } else if (type == PersistentEventType.PreRemove) {
            this.eventPublisher.publishEvent(new GroupRemoved(entity));
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
