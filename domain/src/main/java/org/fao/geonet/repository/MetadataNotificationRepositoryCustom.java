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

package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;

import java.util.List;

/**
 * Custom queries for MetadataNotificationRepository.. User: Jesse Date: 9/7/13 Time: 8:29 PM
 */
public interface MetadataNotificationRepositoryCustom {
    /**
     * Find all the notification that have not yet been sent for a particular notifier.
     *
     * @param notifierId the notifier in question.
     * @param actions    the permitted actions.  these will be turned into an IN clause.  If not
     *                   actions are specified then all actions are accepted
     * @return all the notification that have not yet been sent for a particular notifier.
     */
    List<MetadataNotification> findAllNotNotifiedForNotifier(int notifierId, MetadataNotificationAction... actions);

    /**
     * Delete all notifications with the provided notifierId.
     *
     * @param notifierId the notifier id
     * @return the number of notifications deleted.
     */
    int deleteAllWithNotifierId(int notifierId);
}
