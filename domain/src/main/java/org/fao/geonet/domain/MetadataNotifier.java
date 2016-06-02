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

import org.fao.geonet.entitylistener.MetadataNotifierEntityListenerManager;

import javax.annotation.Nullable;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * An entity representing a service that desires to be notified when a metadata is modified.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "MetadataNotifiers")
@EntityListeners(MetadataNotifierEntityListenerManager.class)
@SequenceGenerator(name = MetadataNotifier.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class MetadataNotifier extends GeonetEntity {
    static final String ID_SEQ_NAME = "metadata_notifier_id_seq";

    private int _id;
    private String _name;
    private String _url;
    private char _enabled = Constants.YN_FALSE;
    private String _username;
    private char[] _password;
    private List<MetadataNotification> _notifications = new ArrayList<MetadataNotification>();

    /**
     * Get the id of this notifier. This is a generated value and as such new instances should not
     * have this set as it will simply be ignored and could result in reduced performance.
     *
     * @return the id of this notifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of this notifier. This is a generated value and as such new instances should not
     * have this set as it will simply be ignored and could result in reduced performance.
     *
     * @param id the id of this notifier
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the "name" (human readable description) of the notifier.
     *
     * @return the "name" (human readable description) of the notifier.
     */
    @Column(nullable = false, length = 32)
    public String getName() {
        return _name;
    }

    /**
     * Set the "name" (human readable description) of the notifier.
     *
     * @param name the name of the notifier
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Get the URL to use to notify the notifier. This is a required non-null property.
     *
     * @return the URL to use to notify the notifier.
     */
    @Column(nullable = false)
    public String getUrl() {
        return _url;
    }

    /**
     * Set the URL to use to notify the notifier. This is a required non-null property.
     *
     * @param url the URL to use to notify the notifier.
     */
    public void setUrl(String url) {
        this._url = url;
    }

    /**
     * For backwards compatibility we need the enabled column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "enabled", length = 1, nullable = false)
    protected char getEnabled_JPAWorkaround() {
        return _enabled;
    }

    /**
     * Set the enabled column value as either Constants.YN_ENABLED or Constants.YN_DISABLED
     *
     * @param enabled either Constants.YN_ENABLED for true or Constants.YN_DISABLED for false
     */
    protected void setEnabled_JPAWorkaround(char enabled) {
        this._enabled = enabled;
    }

    /**
     * Return true if the notifier is enabled and should be notified of changes.
     *
     * @return true if the notifier is enabled and should be notified of changes.
     */
    @Transient
    public boolean isEnabled() {
        return Constants.toBoolean_fromYNChar(getEnabled_JPAWorkaround());
    }

    /**
     * Set true if the notifier is enabled and should be notified of changes.
     *
     * @param enabled true if the notifier is enabled and should be notified of changes.
     */
    public void setEnabled(boolean enabled) {
        setEnabled_JPAWorkaround(Constants.toYN_EnabledChar(enabled));
    }

    /**
     * Get the username to use as credentials when notifying the notifier. This may be null.
     *
     * @return the username to use as credentials when notifying the notifier. This may be null.
     */
    @Column(length = 32)
    @Nullable
    public String getUsername() {
        return _username;
    }

    /**
     * Set the username to use as credentials when notifying the notifier. This may be null.
     *
     * @param username the username to use as credentials when notifying the notifier. This may be
     *                 null.
     */
    public void setUsername(@Nullable String username) {
        this._username = username;
    }

    /**
     * Get the password to use as credentials when notifying the notifier. This may be null.
     *
     * @return the password to use as credentials when notifying the notifier. This may be null.
     */
    @Nullable
    public char[] getPassword() {
        if (_password == null) {
            return null;
        } else {
            return _password.clone();
        }
    }

    /**
     * Set the password to use as credentials when notifying the notifier. This may be null.
     *
     * @param password the password to use as credentials when notifying the notifier. This may be
     *                 null.
     */
    public void setPassword(@Nullable String password) {
        if (password == null) {
            this._password = null;
        } else {
            this._password = password.toCharArray().clone();
        }
    }

    /**
     * Set the password to use as credentials when notifying the notifier. This may be null.
     *
     * @param password the password to use as credentials when notifying the notifier. This may be
     *                 null.
     */
    public void setPassword(@Nullable char[] password) {
        if (password == null) {
            this._password = null;
        } else {
            this._password = password.clone();
        }
    }

    /**
     * Get the lazily loaded list of all the notifications for this notifier. <p> For performance on
     * might use the {@link org.fao.geonet.repository.MetadataNotificationRepository} to efficiently
     * look up just the notifications needed. </p>
     *
     * @return the lazily loaded list of all the notifications for this notifier.
     */
    @JoinColumn(name = "notifierId")
    @OneToMany(fetch = FetchType.LAZY)
    public List<MetadataNotification> getNotifications() {
        return _notifications;
    }

    /**
     * Set the notifications for this metadata notifier
     *
     * @param notifications the notifications
     */
    protected void setNotifications(List<MetadataNotification> notifications) {
        this._notifications = notifications;
    }

}
