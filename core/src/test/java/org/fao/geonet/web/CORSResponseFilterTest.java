/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
package org.fao.geonet.web;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.fao.geonet.web.CORSResponseFilter.ALLOWED_HOSTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by francois on 07/11/16.
 */
public class CORSResponseFilterTest {

    private CORSResponseFilter buildFilter(String allowedHosts) {
        CORSResponseFilter filter = new CORSResponseFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter(ALLOWED_HOSTS, allowedHosts);
        filter.init(filterConfig);

        SettingManager settingManager = mock(SettingManager.class);
        when(settingManager.getValue(Settings.SYSTEM_SERVER_HOST))
                .thenReturn("server.host");
        when(settingManager.getValue(Settings.SYSTEM_CORS_ALLOWEDHOSTS))
                .thenReturn("www.geonetwork-opensource.org,osgeo.org");

        final ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
        ApplicationContextHolder.set(applicationContext);
        Mockito.when(applicationContext.getBean(SettingManager.class)).thenReturn(settingManager);

        return filter;
    }

    @Test
    public void testCorsHeaderNotAddedWhenNone() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = spy(new MockHttpServletResponse());
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getHeader("origin")).thenReturn("http://www.geonetwork-opensource.org");
        buildFilter("").doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertNull(httpServletResponse.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void testCorsHeaderAddedToAllWhenStar() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = spy(new MockHttpServletResponse());
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getHeader("origin")).thenReturn("http://www.geonetwork-opensource.org");
        buildFilter("*").doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertEquals("*", httpServletResponse.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void testCorsHeaderAddedToAllowedHostOnly() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = spy(new MockHttpServletResponse());
        FilterChain filterChain = mock(FilterChain.class);


        CORSResponseFilter filter = buildFilter("www.geonetwork-opensource.org,osgeo.org");
        when(httpServletRequest.getHeader("origin")).thenReturn("http://www.geonetwork-opensource.org");
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertEquals("*", httpServletResponse.getHeader("Access-Control-Allow-Origin"));

        httpServletResponse = spy(new MockHttpServletResponse());
        when(httpServletRequest.getHeader("origin")).thenReturn("http://osgeo.org");
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertEquals("*", httpServletResponse.getHeader("Access-Control-Allow-Origin"));

        httpServletResponse = spy(new MockHttpServletResponse());
        when(httpServletRequest.getHeader("origin")).thenReturn("http://www.dummy.org");
        filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertNull(httpServletResponse.getHeader("Access-Control-Allow-Origin"));
    }


    @Test
    public void testCorsHeaderAddedBasedOnDb() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = spy(new MockHttpServletResponse());
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getHeader("origin")).thenReturn("http://www.geonetwork-opensource.org");
        buildFilter("db").doFilter(httpServletRequest, httpServletResponse, filterChain);
        assertEquals("*", httpServletResponse.getHeader("Access-Control-Allow-Origin"));
    }
}
