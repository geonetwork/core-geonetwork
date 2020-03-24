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

package org.fao.geonet.entitylistener;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Obtains the current node id from the application context and sets it on the user object.
 * <p>
 * User: Jesse Date: 11/26/13 Time: 12:17 PM
 */
public class UserNodeIdSetter implements GeonetworkEntityListener<User> {
    @Autowired
    private ApplicationContext context;

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void handleEvent(final PersistentEventType type, final User entity) {
        if (type == PersistentEventType.PostLoad || type == PersistentEventType.PostPersist || type == PersistentEventType.PrePersist) {
            entity.getSecurity().setNodeId(context.getBean(NodeInfo.class).getId());
        }
    }
}
