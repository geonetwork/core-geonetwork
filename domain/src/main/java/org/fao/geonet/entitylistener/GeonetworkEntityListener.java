package org.fao.geonet.entitylistener;

/**
 * The interface for objects that are interested in entity events.
 *
 * User: Jesse
 * Date: 11/26/13
 * Time: 11:43 AM
 */
public interface GeonetworkEntityListener<T> {
    public Class<T> getEntityClass();
    public void handleEvent(PersistentEventType type, T entity);
}
