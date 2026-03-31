/*
 * Copyright (C) 2026 Food and Agriculture Organization of the
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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class GeonetworkOAuth2LoginAuthenticationFilterTest {

    @Test
    public void sendSafeRedirectAcceptsRelativePath() throws Exception {
        MockHttpServletRequest request = createRequest();

        String result = redirectTo("/geonetwork/srv/eng/catalog.search#/map", request);

        assertEquals("/geonetwork/srv/eng/catalog.search#/map", result);
    }

    @Test
    public void sendSafeRedirectAcceptsSameOriginAbsoluteUrl() throws Exception {
        MockHttpServletRequest request = createRequest();

        String result = redirectTo(
            "https://localhost:8443/geonetwork/srv/eng/catalog.search?foo=bar",
            request
        );

        assertEquals("/geonetwork/srv/eng/catalog.search?foo=bar", result);
    }

    @Test
    public void sendSafeRedirectRejectsExternalAbsoluteUrl() throws Exception {
        MockHttpServletRequest request = createRequest();

        String result = redirectTo(
            "https://evil.example/geonetwork/srv/eng/catalog.search",
            request
        );

        assertEquals("/", result);
    }

    @Test
    public void sendSafeRedirectRejectsProtocolRelativeUrl() throws Exception {
        MockHttpServletRequest request = createRequest();

        String result = redirectTo("//evil.example/path", request);

        assertEquals("/", result);
    }

    @Test
    public void sendSafeRedirectAcceptsPathOutsideContext() throws Exception {
        MockHttpServletRequest request = createRequest();

        String result = redirectTo("/other/srv/eng/main.home", request);

        assertEquals("/other/srv/eng/main.home", result);
    }

    private String redirectTo(String redirectUrlString, MockHttpServletRequest request) throws Exception {
        GeonetworkOAuth2LoginAuthenticationFilter filter = new GeonetworkOAuth2LoginAuthenticationFilter(
            mock(ClientRegistrationRepository.class),
            mock(OAuth2AuthorizedClientService.class)
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        Method method = GeonetworkOAuth2LoginAuthenticationFilter.class.getDeclaredMethod(
            "sendSafeRedirect",
            javax.servlet.http.HttpServletRequest.class,
            javax.servlet.http.HttpServletResponse.class,
            String.class
        );
        method.setAccessible(true);
        method.invoke(filter, request, response, redirectUrlString);
        return response.getRedirectedUrl();
    }

    private MockHttpServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/geonetwork/srv/eng/main.home");
        request.setContextPath("/geonetwork");
        request.setScheme("https");
        request.setServerName("localhost");
        request.setServerPort(8443);
        return request;
    }
}
