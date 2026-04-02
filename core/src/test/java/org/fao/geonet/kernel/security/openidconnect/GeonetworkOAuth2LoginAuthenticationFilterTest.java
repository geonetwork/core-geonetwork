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
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GeonetworkOAuth2LoginAuthenticationFilterTest {

    @Test
    public void resolvePostLoginTargetUsesValidSavedRequestRedirect() {
        MockHttpServletRequest request = createRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        GeonetworkOAuth2LoginAuthenticationFilter filter = createFilter();

        RequestCache requestCache = mock(RequestCache.class);
        SavedRequest savedRequest = mock(SavedRequest.class);
        when(savedRequest.getRedirectUrl()).thenReturn("/geonetwork/srv/eng/main.home");
        when(requestCache.getRequest(request, response)).thenReturn(savedRequest);
        filter.requestCache = requestCache;

        String target = filter.resolvePostLoginTarget(request, response);

        assertEquals("/geonetwork/srv/eng/main.home", target);
        verify(requestCache).removeRequest(request, response);
    }

    @Test
    public void resolvePostLoginTargetFallsBackToValidQueryParam() {
        MockHttpServletRequest request = createRequest();
        request.setQueryString("redirectUrl=%2Fgeonetwork%2Fsrv%2Feng%2Fcatalog.search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        GeonetworkOAuth2LoginAuthenticationFilter filter = createFilter();

        RequestCache requestCache = mock(RequestCache.class);
        when(requestCache.getRequest(request, response)).thenReturn(null);
        filter.requestCache = requestCache;

        String target = filter.resolvePostLoginTarget(request, response);

        assertEquals("/geonetwork/srv/eng/catalog.search", target);
        verify(requestCache, never()).removeRequest(request, response);
    }

    @Test
    public void resolvePostLoginTargetUsesQueryParamWhenSavedRequestIsInvalid() {
        MockHttpServletRequest request = createRequest();
        request.setQueryString("redirectUrl=%2Fgeonetwork%2Fsrv%2Feng%2Fcatalog.search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        GeonetworkOAuth2LoginAuthenticationFilter filter = createFilter();

        RequestCache requestCache = mock(RequestCache.class);
        SavedRequest savedRequest = mock(SavedRequest.class);
        when(savedRequest.getRedirectUrl()).thenReturn("https://evil.example/phish");
        when(requestCache.getRequest(request, response)).thenReturn(savedRequest);
        filter.requestCache = requestCache;

        String target = filter.resolvePostLoginTarget(request, response);

        assertEquals("/geonetwork/srv/eng/catalog.search", target);
        verify(requestCache).removeRequest(request, response);
    }

    @Test
    public void resolvePostLoginTargetPrioritizesSavedRequestOverQueryParam() {
        MockHttpServletRequest request = createRequest();
        request.setQueryString("redirectUrl=%2Fgeonetwork%2Fsrv%2Feng%2Fcatalog.search");
        MockHttpServletResponse response = new MockHttpServletResponse();
        GeonetworkOAuth2LoginAuthenticationFilter filter = createFilter();

        RequestCache requestCache = mock(RequestCache.class);
        SavedRequest savedRequest = mock(SavedRequest.class);
        when(savedRequest.getRedirectUrl()).thenReturn("/geonetwork/srv/eng/main.home");
        when(requestCache.getRequest(request, response)).thenReturn(savedRequest);
        filter.requestCache = requestCache;

        String target = filter.resolvePostLoginTarget(request, response);

        assertEquals("/geonetwork/srv/eng/main.home", target);
        verify(requestCache).removeRequest(request, response);
    }

    @Test
    public void resolveSafeRedirectTargetAcceptsRelativePathInContext() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "/geonetwork/srv/eng/catalog.search#/map"
        );

        assertEquals("/geonetwork/srv/eng/catalog.search#/map", result);
    }

    @Test
    public void resolveSafeRedirectTargetAcceptsSameOriginAbsoluteUrl() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "https://localhost:8443/geonetwork/srv/eng/catalog.search?foo=bar"
        );

        assertEquals("/geonetwork/srv/eng/catalog.search?foo=bar", result);
    }

    @Test
    public void resolveSafeRedirectTargetRejectsExternalAbsoluteUrl() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "https://evil.example/geonetwork/srv/eng/catalog.search"
        );

        assertEquals("/geonetwork", result);
    }

    @Test
    public void resolveSafeRedirectTargetRejectsProtocolRelativeUrl() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "//evil.example/path"
        );

        assertEquals("/geonetwork", result);
    }

    @Test
    public void resolveSafeRedirectTargetRejectsPathOutsideContext() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "/other/srv/eng/main.home"
        );

        assertEquals("/geonetwork", result);
    }

    @Test
    public void resolveSafeRedirectTargetRejectsSameHostButWrongScheme() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "http://localhost:8443/geonetwork/srv/eng/main.home"
        );

        assertEquals("/geonetwork", result);
    }

    @Test
    public void resolveSafeRedirectTargetRejectsSameHostButWrongPort() {
        MockHttpServletRequest request = createRequest();

        String result = GeonetworkOAuth2LoginAuthenticationFilter.resolveSafeRedirectTarget(
            request,
            "https://localhost:443/geonetwork/srv/eng/main.home"
        );

        assertEquals("/geonetwork", result);
    }

    private GeonetworkOAuth2LoginAuthenticationFilter createFilter() {
        return new GeonetworkOAuth2LoginAuthenticationFilter(
            mock(ClientRegistrationRepository.class),
            mock(OAuth2AuthorizedClientService.class)
        );
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
