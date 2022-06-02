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
package org.fao.geonet.kernel.security.openidconnect;

import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class OAuth2SecurityProviderUtil implements SecurityProviderUtil {

    @Autowired
    OidcUser2GeonetworkUser oidcUser2GeonetworkUser;


    public String getSSOAuthenticationHeaderValue() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return "Bearer " + user.getIdToken().getTokenValue();
        }
        return null;
    }

    public UserDetails getUserDetails(Authentication auth) {
        return getUserDetails(auth, false);
    }

    public UserDetails getUserDetails(Authentication auth, boolean withDbUpdate) {
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            OidcIdToken idToken = user.getIdToken();
            return oidcUser2GeonetworkUser.getUserDetails(idToken, withDbUpdate);

            // return keycloakUserUtils.getUserDetails(((KeycloakPrincipal) auth.getPrincipal()).getKeycloakSecurityContext().getToken(), false);
        } else {
            // If unknown auth class then return null.
            // This will occur when it is an anonymous user.
            return null;
        }
    }
}
