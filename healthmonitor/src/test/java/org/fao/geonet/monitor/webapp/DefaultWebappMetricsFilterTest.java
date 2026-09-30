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

package org.fao.geonet.monitor.webapp;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

public class DefaultWebappMetricsFilterTest {

    private MetricsRegistry registry;
    private DefaultWebappMetricsFilter filter;

    @Before
    public void createFilter() throws Exception {
        MockServletContext servletContext = new MockServletContext();
        registry = new MetricsRegistry();
        servletContext.setAttribute(DefaultWebappMetricsFilter.REGISTRY_ATTRIBUTE, registry);
        filter = new DefaultWebappMetricsFilter();
        filter.init(new MockFilterConfig(servletContext));
    }

    @Test
    public void responseThatKeepsTheDefaultStatusIsCountedAsOk() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> { });

        assertEquals(1, count("responseCodes.ok"));
        assertEquals(0, count("responseCodes.other"));
    }

    @Test
    public void codeBehindTheFilterSeesTheStatusOfTheResponse() throws Exception {
        int[] status = new int[1];
        FilterChain chain = (request, response) -> status[0] = ((HttpServletResponse) response).getStatus();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertEquals(HttpServletResponse.SC_OK, status[0]);
    }

    @Test
    public void responseWithAnErrorIsCountedWithItsStatus() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
            (request, response) -> ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND));

        assertEquals(1, count("responseCodes.notFound"));
        assertEquals(0, count("responseCodes.ok"));
    }

    private long count(String name) {
        return ((Meter) registry.allMetrics().get(new MetricName(WebappMetricsFilter.class, name))).count();
    }
}
