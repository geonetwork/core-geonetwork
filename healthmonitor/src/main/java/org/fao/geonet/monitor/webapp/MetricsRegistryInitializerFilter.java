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

package org.fao.geonet.monitor.webapp;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

import jeeves.monitor.MonitorManager;
import org.fao.geonet.utils.Log;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * Sets the metrics registries earlier enough so all geonetwork and metrics will get and use the
 * same instance.
 *
 * User: jeichar Date: 4/17/12 Time: 5:32 PM
 */
public class MetricsRegistryInitializerFilter implements Filter {
    private MetricsRegistry metricsRegistry;

    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        context.setAttribute(MonitorManager.HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.CRITICAL_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.WARNING_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.EXPENSIVE_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());

        metricsRegistry = new MetricsRegistry();
        context.setAttribute(MonitorManager.METRICS_REGISTRY, metricsRegistry);
        context.setAttribute(DefaultWebappMetricsFilter.REGISTRY_ATTRIBUTE, metricsRegistry);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public void destroy() {
        Log.info(Log.WEBAPP, "Shutdown metricsRegistry");
        metricsRegistry.shutdown();
    }
}
