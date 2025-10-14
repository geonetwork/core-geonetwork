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
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import static org.fao.geonet.kernel.security.openidconnect.bearer.OIDCServiceAccountLogin.PRINCIPAL_NAME;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OIDCServiceAccountLoginTest {

    private OAuth2AuthorizedClientManager clientManager;
    private GeonetworkClientRegistrationRepository registrations;
    private GeonetworkJwtAuthenticationProvider jwtProvider;
    private OIDCServiceAccountLogin serviceAccountLogin;

    @Before
    public void setUp() {
        clientManager = mock(OAuth2AuthorizedClientManager.class);
        registrations = mock(GeonetworkClientRegistrationRepository.class);
        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        jwtProvider = mock(GeonetworkJwtAuthenticationProvider.class);

        when(registrations.findByRegistrationId(any())).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(PRINCIPAL_NAME);

    }

    @Test
    public void testLoginServiceAccountSuccess() {
        serviceAccountLogin = new OIDCServiceAccountLogin(clientManager, registrations, jwtProvider);

        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        Authentication authentication = mock(Authentication.class);

        when(clientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "tokenValue", null, null));
        when(jwtProvider.authenticate(any(), any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        boolean result = serviceAccountLogin.loginServiceAccount();
        assertTrue(result);
    }

    @Test(expected = IllegalStateException.class)
    public void testLoginServiceAccountAuthorizationFails() {
        serviceAccountLogin = new OIDCServiceAccountLogin(clientManager, registrations, jwtProvider);
        when(clientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);
        serviceAccountLogin.loginServiceAccount();
    }

    @Test
    public void testLoginServiceAccountJwtProviderNull() {
        serviceAccountLogin = new OIDCServiceAccountLogin(clientManager, registrations, null);
        boolean result = serviceAccountLogin.loginServiceAccount();
        assertFalse(result);
    }
}
