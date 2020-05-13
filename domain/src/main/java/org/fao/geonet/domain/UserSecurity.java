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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Encapsulates security information about the user. This is a JPA Embeddable object that is
 * embedded into a {@link User} Entity
 *
 * @author Jesse
 */
@Embeddable
public class UserSecurity extends GeonetEntity implements Serializable {
    private char[] _password;
    private Set<UserSecurityNotification> _securityNotifications = new HashSet<UserSecurityNotification>();
    private String _authType;
    private String _nodeId;

    /**
     * Get the hashed password. This is a required property.
     *
     * @return the hashed password
     */
    @Column(nullable = false, length = 120)
    @Nonnull
    public char[] getPassword() {
        return _password == null ? new char[0] : _password.clone();
    }

    /**
     * Set the hashed password. This is a required property.
     *
     * @param password the hashed password.
     * @return this UserSecurity object
     */
    public
    @Nonnull
    @JsonIgnore
    UserSecurity setPassword(@Nonnull char[] password) {
        this._password = password == null ? new char[0] : password.clone();
        return this;
    }

    /**
     * Set the hashed password. This is a required property.
     *
     * @param password the hashed password.
     * @return this UserSecurity object
     */
    public UserSecurity setPassword(String password) {
        setPassword(password.toCharArray());
        return this;
    }

    /**
     * Get the security notifications. This property used to store arbitrary security related
     * notifications.
     */
    @Column(name = "security", length = 128)
    protected String getSecurityNotificationsString() {
        StringBuilder builder = new StringBuilder();
        for (UserSecurityNotification not : _securityNotifications) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(not.toString());
        }
        return builder.toString();
    }

    protected UserSecurity setSecurityNotificationsString(final String securityNotifications) {
        _securityNotifications.clear();
        if (securityNotifications != null) {
            String[] parts = securityNotifications.split(",");

            for (String string : parts) {
                if (!string.trim().isEmpty()) {
                    _securityNotifications.add(UserSecurityNotification.find(string));
                }
            }
        }
        return this;
    }

    /**
     * Get the mutable set if security notifications.
     *
     * @return the mutable set if security notifications.
     */
    @Transient
    public Set<UserSecurityNotification> getSecurityNotifications() {
        return _securityNotifications;
    }

    @Column(name = "authtype", length = 32)
    public String getAuthType() {
        return _authType;
    }

    public UserSecurity setAuthType(String authType) {
        this._authType = authType;
        return this;
    }

    /**
     * Merge all data from other security into this security.
     *
     * @param otherSecurity other user to merge data from.
     * @param mergeNullData if true then also set null values from other security. If false then
     *                      only merge non-null data
     */
    public void mergeSecurity(UserSecurity otherSecurity, boolean mergeNullData) {
        if (mergeNullData || otherSecurity.getPassword() != null) {
            setPassword(otherSecurity.getPassword());
        }
        if (mergeNullData || otherSecurity.getSecurityNotifications() != null) {
            setSecurityNotificationsString(otherSecurity.getSecurityNotificationsString());
        }
        if (mergeNullData || otherSecurity.getAuthType() != null) {
            setAuthType(otherSecurity.getAuthType());
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSecurity that = (UserSecurity) o;

        if (_authType != null ? !_authType.equals(that._authType) : that._authType != null)
            return false;
        if (_nodeId != null ? !_nodeId.equals(that._nodeId) : that._nodeId != null) return false;
        if (!Arrays.equals(_password, that._password)) return false;
        if (!_securityNotifications.equals(that._securityNotifications)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _password != null ? Arrays.hashCode(_password) : 0;
        result = 31 * result + _securityNotifications.hashCode();
        result = 31 * result + (_authType != null ? _authType.hashCode() : 0);
        result = 31 * result + (_nodeId != null ? _nodeId.hashCode() : 0);
        return result;
    }

    /**
     * Get the id of the node this user was loaded from.
     *
     * @return the id of the node this user was loaded from.
     */
    public String getNodeId() {
        return _nodeId;
    }

    /**
     * Set id of the node this user was loaded from.
     *
     * @param associatedNode id of the node this user was loaded from.
     */
    public void setNodeId(final String associatedNode) {
        this._nodeId = associatedNode;
    }

    @Override
    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        final Element element = super.asXml(alreadyEncoded);
        element.removeChild("password");
        return element;
    }
}
