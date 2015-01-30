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
 * User: Jesse
 * Date: 11/26/13
 * Time: 11:33 AM
 */
public class AbstractEntityListenerManager<T> {
    protected void handleEvent(final PersistentEventType type, final T entity) {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        if (context != null) {
            final Map<String,GeonetworkEntityListener> listeners = context.getBeansOfType(GeonetworkEntityListener.class);
            for (GeonetworkEntityListener listener : listeners.values()) {
                if (listener.getEntityClass() == entity.getClass()) {
                    listener.handleEvent(type, entity);
                }
            }
        } else {
            Log.warning(Constants.DOMAIN_LOG_MODULE, "An event occurred that was not handled because the " +
                                                     ApplicationContextHolder.class.getName() +
                                                     " has not been set in this thread");
        }
    }
}
