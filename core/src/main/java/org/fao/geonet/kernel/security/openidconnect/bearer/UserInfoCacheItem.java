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
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collection;

/**
 * Cached info about the User (used and resolved by the JwtAuthenticationProvider).
 */
public class UserInfoCacheItem {

    public String accessToken;
    public Instant expireTime;
    public OAuth2User user;
    Collection<? extends GrantedAuthority> authorities;

    public UserInfoCacheItem(String accessToken, Instant expireTime, OAuth2User user, Collection<? extends GrantedAuthority> authorities) {
        this.accessToken = accessToken;
        this.user = user;
        this.expireTime = expireTime;
        this.authorities = authorities;
    }

    public boolean isExpired() {
        return (expireTime.compareTo(Instant.now()) < 0);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Instant getExpireTime() {
        return expireTime;
    }

    public OAuth2User getUser() {
        return user;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
