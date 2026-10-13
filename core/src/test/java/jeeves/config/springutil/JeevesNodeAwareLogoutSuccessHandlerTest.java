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
package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JeevesNodeAwareLogoutSuccessHandlerTest {

    private JeevesNodeAwareLogoutSuccessHandler handler;
    private SettingManager settingManager;
    private MockedStatic<JeevesDelegatingFilterProxy> proxyStatic;

    @Before
    public void setUp() {
        handler = new JeevesNodeAwareLogoutSuccessHandler();
        handler.setTargetUrlParameter("redirectUrl");
        handler.setDefaultTargetUrl("/srv/@@nodeId@@/catalog.search");

        settingManager = mock(SettingManager.class);
        when(settingManager.getValue(Settings.SYSTEM_SERVER_HOST)).thenReturn("www.example.org");
        when(settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL)).thenReturn("https");
        when(settingManager.getServerPort()).thenReturn(443);

        ReflectionTestUtils.setField(handler, "settingManager", settingManager);
        ReflectionTestUtils.setField(handler, "context", mock(ServletContext.class));

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId("srv");
        ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
        when(applicationContext.getBean(NodeInfo.class)).thenReturn(nodeInfo);

        proxyStatic = org.mockito.Mockito.mockStatic(JeevesDelegatingFilterProxy.class);
        proxyStatic.when(() -> JeevesDelegatingFilterProxy.getServletContext(any()))
            .thenReturn(mock(ServletContext.class));
        proxyStatic.when(() -> JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(any()))
            .thenReturn(applicationContext);
    }

    @After
    public void tearDown() {
        proxyStatic.close();
    }

    private String determineTargetUrl(String redirectUrlParam) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("redirectUrl")).thenReturn(redirectUrlParam);
        return (String) ReflectionTestUtils.invokeMethod(handler, "determineTargetUrl", request, response);
    }

    @Test
    public void relativeTargetIsHonoured() {
        assertEquals("/portal/srv/eng/catalog.search", determineTargetUrl("/portal/srv/eng/catalog.search"));
    }

    @Test
    public void sameSiteAbsoluteTargetIsHonoured() {
        assertEquals("https://www.example.org/portal", determineTargetUrl("https://www.example.org/portal"));
    }

    @Test
    public void protocolRelativeTargetFallsBackToDefault() {
        // The //host bypass: starts with "/" but resolves to an external host.
        assertEquals("/srv/srv/catalog.search", determineTargetUrl("//evil.example.com"));
    }

    @Test
    public void backslashProtocolRelativeTargetFallsBackToDefault() {
        assertEquals("/srv/srv/catalog.search", determineTargetUrl("/\\evil.example.com"));
    }

    @Test
    public void crossSiteAbsoluteTargetFallsBackToDefault() {
        assertEquals("/srv/srv/catalog.search", determineTargetUrl("https://evil.example.com/phish"));
    }

    @Test
    public void noRedirectParameterUsesDefaultTarget() {
        // Default target has its @@nodeId@@ placeholder substituted with the node id.
        assertEquals("/srv/srv/catalog.search", determineTargetUrl(null));
    }
}
