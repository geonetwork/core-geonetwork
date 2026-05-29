/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedirectUtilTest {

    @Test
    public void serverLocalPathsAreSafe() {
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork"));
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork/srv/eng/catalog.search"));
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork/srv/eng/catalog.search#/home"));
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork/srv/eng/catalog.search?a=b&c=d"));
    }

    @Test
    public void emptyOrNullIsNotSafe() {
        assertFalse(RedirectUtil.isSafeRedirect(null));
        assertFalse(RedirectUtil.isSafeRedirect(""));
    }

    @Test
    public void absoluteUrlsAreNotSafe() {
        assertFalse(RedirectUtil.isSafeRedirect("http://evil.example.com"));
        assertFalse(RedirectUtil.isSafeRedirect("https://evil.example.com/path"));
        assertFalse(RedirectUtil.isSafeRedirect("javascript:alert(1)"));
    }

    @Test
    public void protocolRelativeUrlsAreNotSafe() {
        // No scheme, so URI.isAbsolute() returns false, but the browser still
        // resolves these to an external host.
        assertFalse(RedirectUtil.isSafeRedirect("//evil.example.com"));
        assertFalse(RedirectUtil.isSafeRedirect("//evil.example.com/path"));
        assertFalse(RedirectUtil.isSafeRedirect("/\\evil.example.com"));
        assertFalse(RedirectUtil.isSafeRedirect("\\\\evil.example.com"));
        assertFalse(RedirectUtil.isSafeRedirect("\\/evil.example.com"));
    }

    @Test
    public void pathsNotAnchoredToHostAreNotSafe() {
        assertFalse(RedirectUtil.isSafeRedirect("evil.example.com"));
        assertFalse(RedirectUtil.isSafeRedirect("catalog.search"));
    }

    @Test
    public void sameSiteAbsoluteUrlsAreSafe() {
        // Relative server-local paths remain safe regardless of site settings.
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork/srv/eng/catalog.search",
            "www.example.org", "https", 443));

        // Absolute URLs to the same host/protocol/port are accepted.
        assertTrue(RedirectUtil.isSafeRedirect("https://www.example.org/geonetwork/srv/eng/catalog.search",
            "www.example.org", "https", 443));
        assertTrue(RedirectUtil.isSafeRedirect("https://www.example.org:8443/geonetwork",
            "www.example.org", "https", 8443));
        // Host comparison is case-insensitive.
        assertTrue(RedirectUtil.isSafeRedirect("https://WWW.EXAMPLE.ORG/geonetwork",
            "www.example.org", "https", 443));
    }

    @Test
    public void differentSiteAbsoluteUrlsAreNotSafe() {
        assertFalse(RedirectUtil.isSafeRedirect("https://evil.example.com/geonetwork",
            "www.example.org", "https", 443));
        // Same host but different protocol.
        assertFalse(RedirectUtil.isSafeRedirect("http://www.example.org/geonetwork",
            "www.example.org", "https", 443));
        // Same host but different port.
        assertFalse(RedirectUtil.isSafeRedirect("https://www.example.org:9999/geonetwork",
            "www.example.org", "https", 443));
        // Protocol-relative URLs are still rejected even with site settings.
        assertFalse(RedirectUtil.isSafeRedirect("//evil.example.com",
            "www.example.org", "https", 443));
    }

    @Test
    public void sameSiteCheckIsSafeWhenSiteSettingsAreIncomplete() {
        // Relative paths still pass, absolute URLs cannot be validated and are rejected.
        assertTrue(RedirectUtil.isSafeRedirect("/geonetwork", null, null, null));
        assertFalse(RedirectUtil.isSafeRedirect("https://www.example.org/geonetwork", null, null, null));
        assertFalse(RedirectUtil.isSafeRedirect("https://www.example.org/geonetwork",
            "www.example.org", "https", null));
    }

    @Test
    public void sendSafeRedirectFollowsSafeTarget() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/geonetwork");

        RedirectUtil.sendSafeRedirect(request, response, "/geonetwork/srv/eng/catalog.search");

        verify(response).sendRedirect("/geonetwork/srv/eng/catalog.search");
    }

    @Test
    public void sendSafeRedirectFallsBackToContextHomeForUnsafeTarget() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/geonetwork");

        RedirectUtil.sendSafeRedirect(request, response, "//evil.example.com");

        verify(response).sendRedirect("/geonetwork");
    }

    @Test
    public void sendSafeRedirectFallsBackToContextHomeForNullTarget() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("/geonetwork");

        RedirectUtil.sendSafeRedirect(request, response, null);

        verify(response).sendRedirect("/geonetwork");
    }
}
