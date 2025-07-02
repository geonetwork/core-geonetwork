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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * This implements GN's SecurityProviderUtil.
 * <p>
 * Based on GN's Keycloak security plugin.
 */
public class OAuth2SecurityProviderUtil implements SecurityProviderUtil {

    @Autowired
    OidcUser2GeonetworkUser oidcUser2GeonetworkUser;

     /**
     * Service to retrieve OAuth2 authorized clients inorder to get access token.
     */
    @Autowired(required = false)
    private OAuth2AuthorizedClientManager authorizedClientManager;


   // setup for BEARER authentication
    public String getSSOAuthenticationHeaderValue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            // Refresh access token before getting new token, otherwise we could be using a token that is about to expire and that could cause issues.
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(oauth2Token.getAuthorizedClientRegistrationId())
                    .principal(oauth2Token)
                    .build()
            );

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                return "Bearer " + authorizedClient.getAccessToken().getTokenValue();
            }else{
                Log.warning(Geonet.GEONETWORK,"Unable to retrieve access token for OAuth2AuthenticationToken. Authorized client is null or access token is null.");
            }

        }
        return null;
    }

    public UserDetails getUserDetails(Authentication auth)  {
        try {
            return getUserDetails(auth, false);
        } catch (Exception e) {
            Log.error(Geonet.SECURITY,"OIDC: couldnt get user details from OIDC user",e);
            return null;
        }
    }

    // get the user's details (spring).  This might update the GN database with the user
    // (see underlying  oidcUser2GeonetworkUser#getUserDetail for when).
    public UserDetails getUserDetails(Authentication auth, boolean withDbUpdate) throws Exception {
        if (auth != null && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            OidcIdToken idToken = user.getIdToken();
            return oidcUser2GeonetworkUser.getUserDetails(idToken, user.getAttributes(), withDbUpdate);
        } else if (auth != null && auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User user = (OAuth2User) auth.getPrincipal();
            return oidcUser2GeonetworkUser.getUserDetails(user.getAttributes(), withDbUpdate);
        } else {
            // If unknown auth class then return null.
            // This will occur when it is an anonymous user.
            return null;
        }
    }


}
