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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * Allow EntityListeners to be registered in the spring security context.
 *
 * @param <T> The type of entity this listener will recieve events from.
 *
 *            User: Jesse Date: 11/26/13 Time: 11:33 AM
 */
public class AbstractEntityListenerManager<T> {
    private static boolean systemRunning = false;

    /**
     * set to true when it should be considered an error when there is no ApplicationContext set in
     * the {@link org.fao.geonet.ApplicationContextHolder}.  For example, when the Web application
     * has been loaded it should call this method to indicate that the system is ready.
     */
    public static void setSystemRunning(boolean systemRunning) {
        AbstractEntityListenerManager.systemRunning = systemRunning;
    }

    protected void handleEvent(final PersistentEventType type, final T entity) {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        if (context != null) {
            final Map<String, GeonetworkEntityListener> listeners = context.getBeansOfType(GeonetworkEntityListener.class);
            for (GeonetworkEntityListener listener : listeners.values()) {
                if (listener.getEntityClass() == entity.getClass()) {
                    listener.handleEvent(type, entity);
                }
            }
        } else if (systemRunning) {
            Log.error(Constants.DOMAIN_LOG_MODULE, "An event occurred that was not handled because the " +
                ApplicationContextHolder.class.getName() +
                " has not been set in this thread", new IllegalStateException("No ApplicationContext set in thread local"));
        }
    }
}
