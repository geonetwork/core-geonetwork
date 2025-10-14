/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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

import org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.fao.geonet.kernel.security.openidconnect.bearer.GeonetworkClientServiceAccountRegistrationProvider.CLIENT_SERVICE_ACCOUNT_REGISTRATION_NAME;

/**
 * Class to perform login of a service account using OAuth2 client credentials flow.
 *
 * Used when making backend to backend calls where no user is involved.
 *
 * Only works with openidconnectbearer configuration as it needs to support bearer tokens.
 */
public class OIDCServiceAccountLogin {

    public final static String PRINCIPAL_NAME="service-account";

    private final OAuth2AuthorizedClientManager clientManager;
    private final ClientRegistration clientRegistration;

    private final GeonetworkJwtAuthenticationProvider geonetworkJwtAuthenticationProvider;

    @Autowired
    public OIDCServiceAccountLogin(OAuth2AuthorizedClientManager clientManager,
                                   GeonetworkClientRegistrationRepository registrations,
                                   @Autowired(required = false) GeonetworkJwtAuthenticationProvider geonetworkJwtAuthenticationProvider) {
        this.clientManager = clientManager;
        clientRegistration = registrations.findByRegistrationId(CLIENT_SERVICE_ACCOUNT_REGISTRATION_NAME);
        this.geonetworkJwtAuthenticationProvider = geonetworkJwtAuthenticationProvider;
    }

    /**
     * Logs in the service account using OAuth2 client credentials flow and sets the authentication in the security context.
     *
     * @return true if the login was successful and the authentication is authenticated, false otherwise.
     * @throws IllegalStateException if the authorization fails.
     */
    public boolean loginServiceAccount() {
        // If GeonetworkJwtAuthenticationProvider is not configured then we cannot log in the service account so return false.
        // This is only supported when using OIDC with bearer tokens.
        if (geonetworkJwtAuthenticationProvider == null) {
            return false;
        }

        OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId(clientRegistration.getRegistrationId())
            .principal(PRINCIPAL_NAME)
            .build();

        OAuth2AuthorizedClient client = clientManager.authorize(oAuth2AuthorizeRequest);
        if (client == null) {
            throw new IllegalStateException("Failed to authorize service account.");
        }

        BearerTokenAuthenticationToken bearerTokenAuthenticationToken = new BearerTokenAuthenticationToken(client.getAccessToken().getTokenValue());

        Authentication authentication = geonetworkJwtAuthenticationProvider.authenticate(bearerTokenAuthenticationToken, clientRegistration.getRegistrationId());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication.isAuthenticated();
    }
}
