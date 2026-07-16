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

import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeonetworkSavedRequestAwareAuthenticationSuccessHandlerTest {

    @Mock
    private SettingManager settingManager;

    private GeonetworkSavedRequestAwareAuthenticationSuccessHandler handler;

    @Before
    public void setUp() {
        handler = new GeonetworkSavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectUrl");
        handler.setDefaultTargetUrl("/");
        ReflectionTestUtils.setField(handler, "settingManager", settingManager);

        lenient().when(settingManager.getValue(Settings.SYSTEM_SERVER_HOST)).thenReturn("www.example.org");
        lenient().when(settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL)).thenReturn("https");
        lenient().when(settingManager.getServerPort()).thenReturn(443);
    }

    private String determineTargetUrl(String redirectUrlParam) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("redirectUrl")).thenReturn(redirectUrlParam);
        return (String) ReflectionTestUtils.invokeMethod(handler, "determineTargetUrl", request, response);
    }

    @Test
    public void relativeTargetIsHonoured() {
        assertEquals("/geonetwork/srv/eng/catalog.search",
            determineTargetUrl("/geonetwork/srv/eng/catalog.search"));
    }

    @Test
    public void sameSiteAbsoluteTargetIsHonoured() {
        assertEquals("https://www.example.org/geonetwork/srv/eng/catalog.search",
            determineTargetUrl("https://www.example.org/geonetwork/srv/eng/catalog.search"));
    }

    @Test
    public void protocolRelativeTargetFallsBackToDefault() {
        assertEquals("/", determineTargetUrl("//evil.example.com"));
    }

    @Test
    public void crossSiteAbsoluteTargetFallsBackToDefault() {
        assertEquals("/", determineTargetUrl("https://evil.example.com/phish"));
    }

    @Test
    public void noRedirectParameterUsesDefaultTarget() {
        assertEquals("/", determineTargetUrl(null));
    }
}
