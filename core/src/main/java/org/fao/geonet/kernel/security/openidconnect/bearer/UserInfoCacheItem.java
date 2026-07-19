/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;

/**
 * Cached info about the User (used and resolved by the JwtAuthenticationProvider).
 * <p>
 * This class stores authentication information for a user including the JWT token,
 * its decoded representation, and the user's granted authorities. It is used to
 * cache authentication data to avoid repeated lookups and validations.
 * </p>
 * <p>
 * The cache item includes expiration checking capabilities based on the JWT's
 * expiration timestamp to ensure cached credentials are not used beyond their
 * validity period.
 * </p>
 *
 * @see org.springframework.security.oauth2.jwt.Jwt
 * @see org.springframework.security.core.GrantedAuthority
 */
public class UserInfoCacheItem {

    /**
     * The raw JWT token string as received from the authentication request.
     */
    public String rawToken;

    /**
     * The decoded and validated JWT object containing claims and metadata.
     */
    public Jwt jwt;

    /**
     * Collection of authorities (roles/permissions) granted to the user.
     */
    Collection<? extends GrantedAuthority> authorities;

    /**
     * The username of the authenticated user, derived from the configured
     * username attribute (e.g., email, preferred_username, or custom claim).
     */
    String username;

    /**
     * Constructs a new UserInfoCacheItem with the specified token, JWT, authorities, and username.
     *
     * @param rawToken     the raw JWT token string
     * @param jwt          the decoded JWT object
     * @param authorities  the collection of granted authorities for the user
     * @param username username of the authenticated user
     */
    public UserInfoCacheItem(String rawToken, Jwt jwt, Collection<? extends GrantedAuthority> authorities, String username) {
        this.rawToken = rawToken;
        this.jwt = jwt;
        this.authorities = authorities;
        this.username = username;
    }

    /**
     * Gets the raw JWT token string.
     *
     * @return the raw token string
     */
    public String getRawToken() {
        return rawToken;
    }

    /**
     * Gets the decoded JWT object.
     *
     * @return the JWT object containing claims and metadata
     */
    public Jwt getJwt() {
        return jwt;
    }

    /**
     * Gets the collection of granted authorities for the user.
     *
     * @return the collection of authorities (roles/permissions)
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Gets the principal name (username) of the authenticated user.
     *
     * @return the principal name derived from the configured username attribute, or null if not set
     */
    public String getUsername() {
        return username;
    }

    /**
     * Checks whether the JWT token has expired.
     * <p>
     * This method compares the JWT's expiration time against the current system time.
     * If the JWT does not have an expiration time set, it is considered not expired.
     * </p>
     *
     * @return {@code true} if the JWT has an expiration time and it is before the current time,
     *         {@code false} otherwise
     */
    public boolean isExpired() {
        return jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now());
    }
}
