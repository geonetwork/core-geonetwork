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

import org.fao.geonet.entitylistener.MetadataNotificationEntityListenerManager;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * An entity representing a metadata related notification that has been made or is pending.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataNotifications")
@EntityListeners(MetadataNotificationEntityListenerManager.class)
public class MetadataNotification extends GeonetEntity {
    private MetadataNotificationId _id;
    private char _notified = Constants.YN_FALSE;
    private String _metadataUuid;
    private MetadataNotificationAction _action;
    private String _errorMessage;

    /**
     * Get the id object for this Notification entity.
     *
     * @return the id object for this Notification entity.
     */
    @EmbeddedId
    public MetadataNotificationId getId() {
        return _id;
    }

    /**
     * Set the id object for this Notification entity.
     *
     * @param id the id object for this Notification entity.
     * @return this notification entity object
     */
    public MetadataNotification setId(MetadataNotificationId id) {
        this._id = id;
        return this;
    }

    /**
     * For backwards compatibility we need the notified column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "notified", length = 1, nullable = false)
    protected char getNotified_JPAWorkaround() {
        return _notified;
    }

    /**
     * Return the value of the notified column.
     *
     * @param notified y or n
     */
    protected void setNotified_JPAWorkaround(char notified) {
        this._notified = notified;
    }

    /**
     * Return true if the notification has been made.
     *
     * @return true if the notification has been made.
     */
    @Transient
    public boolean isNotified() {
        return Constants.toBoolean_fromYNChar(getNotified_JPAWorkaround());
    }

    /**
     * Set true if the notification has been made.
     *
     * @param notified true if the notification has been made.
     * @return this notification entity object
     */
    public MetadataNotification setNotified(boolean notified) {
        setNotified_JPAWorkaround(Constants.toYN_EnabledChar(notified));
        return this;
    }

    /**
     * Get the metadata uuid of the metadata that the notification is for.
     *
     * @return the metadata uuid of the metadata that the notification is for.
     */
    @Column(name = "metadataUuid", nullable = false)
    public String getMetadataUuid() {
        return _metadataUuid;
    }

    /**
     * Set the metadata uuid of the metadata that the notification is for.
     *
     * @param metadataUuid the metadata uuid of the metadata that the notification is for.
     * @return this notification entity object
     */
    public MetadataNotification setMetadataUuid(String metadataUuid) {
        this._metadataUuid = metadataUuid;
        return this;
    }

    /**
     * Get the action performed on the metadata.
     *
     * @return the action performed on the metadata.
     */
    @Column(length = 1, nullable = false)
    public MetadataNotificationAction getAction() {
        return _action;
    }

    /**
     * set the action performed on the metadata.
     *
     * @param action the action performed on the metadata.
     * @return this notification entity object
     */
    public MetadataNotification setAction(MetadataNotificationAction action) {
        this._action = action;
        return this;
    }

    /**
     * Get the error message if any related to the notification.
     *
     * @return the error message if any related to the notification.
     */
    @Lob
    @Column(name = "errormsg")
    @Type(type = "org.hibernate.type.StringClobType")
    // this is a work around for postgres so postgres can correctly load clobs
    public String getErrorMessage() {
        return _errorMessage;
    }

    /**
     * Set the error message if any related to the notification.
     *
     * @param errorMessage the error message if any related to the notification.
     * @return this notification entity object
     */
    public MetadataNotification setErrorMessage(String errorMessage) {
        this._errorMessage = errorMessage;
        return this;
    }

}
