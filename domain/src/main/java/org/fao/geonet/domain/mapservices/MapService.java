/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.domain.mapservices;

import jakarta.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapService {

    public enum AuthType {
        /**
         * Authentication should use a token based on the current authorized user
         */
        BEARER(true),   // Authentication should use a token based on the current authorized user

        /**
         * Authentication should use a username and password based on the supplied
         * username and password in the configurations. For security reason this cannot be used in public
         * client and should only be used via proxy
         */
        BASIC(false);

        private boolean publicClientAllowed;

        AuthType(boolean publicClientAllowed) {
            this.publicClientAllowed = publicClientAllowed;
        }

        public boolean isPublicClientAllowed() {
            return publicClientAllowed;
        }
    }

    public enum UrlType {
        /**
         * Type of url - url or regular expression
         */
        TEXT,
        REGEXP;
    }

    /**
     * URL used to identify if the map server requires authentication
     */
    private String url;

    /**
     * URLType - either TEXT or REGEXP - default to text
     */
    private UrlType urlType = UrlType.TEXT;

    /**
     * use proxy - used to force authentication to use proxy
     */
    private Boolean useProxy = true; // Set proxy default to true

    /**
     * Auth Type - Identify the authentication type that should be used when sending authentication request for the url
     */
    private AuthType authType;

    /**
     * username and password - server credentials to be used for the authentication type - if required.
     */
    private String username;
    private String password;

    @Nonnull
    public String getUrl() {
        return url;
    }

    @Nonnull
    public void setUrl(String url) {
        this.url = url;
    }

    @Nonnull
    public String getUrlType() {
        return urlType.toString();
    }

    @Nonnull
    public void setUrlType(String urlType) {
        this.urlType = UrlType.valueOf(urlType);
    }

    @Nonnull
    public Boolean getUseProxy() {
        // As we cannot expose credentials to the public/javascript client , then proxy will always be true
        if (!authType.isPublicClientAllowed()) {
            return true;
        }
        return useProxy;
    }

    @Nonnull
    public void setUseProxy(boolean useProxy) {
        if (authType != null && !authType.isPublicClientAllowed() && !useProxy) {
            throw new IllegalArgumentException(String.format("Cannot set \"useProxy=false\". Authentication type %s only supports \"useProxy=true\" for security reasons. (%s)", authType, this));
        }
        this.useProxy = useProxy;
    }

    @Nonnull
    public String getAuthType() {
        return authType.toString();
    }

    @Nonnull
    public void setAuthType(String authType) {
        if (authType != null && !AuthType.valueOf(authType).isPublicClientAllowed() && !this.useProxy) {
            throw new IllegalArgumentException(String.format("Authentication type %s only supports \"useProxy=true\" for security reasons.(%s)", authType, this));
        }

        this.authType = AuthType.valueOf(authType);
    }

    @JsonIgnore // Never return username in json results to the api
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore // Never return password in json results to the api
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "MapService{" +
            "url='" + url + '\'' +
            ", urlType=" + urlType +
            ", useProxy=" + useProxy +
            ", authType=" + authType +
            '}';
    }
}
