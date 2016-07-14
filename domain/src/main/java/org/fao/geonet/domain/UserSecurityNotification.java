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

package org.fao.geonet.domain;

public enum UserSecurityNotification {
    /**
     * Indicates the the hash is in need up an update.  Could be because the database has been
     * upgraded and has an old version of the hashcode or a new version or salt has been created.
     */
    UPDATE_HASH_REQUIRED,
    /**
     * Notification that database has an unrecognized notification.  This is an error and the System
     * administrator should review the users table.
     */
    UNKNOWN;

    /**
     * Look up the notification or return the unknown notification if it is not found.
     *
     * @param notificationName the name of the notification to look up.
     */
    public static UserSecurityNotification find(String notificationName) {
        for (UserSecurityNotification notification : values()) {
            if (notification.toString().equalsIgnoreCase(notificationName)) {
                return notification;
            }
        }
        return UNKNOWN;
    }
}
