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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;

/**
 * An HTTP servlet which runs the health checks registered with a given {@link HealthCheckRegistry}
 * and prints the results as a {@code text/plain} entity. Only responds to {@code GET} requests.
 * <p/>
 * If the servlet context has an attribute named {@code com.yammer.metrics.reporting.HealthCheckServlet.registry}
 * which is a {@link HealthCheckRegistry} instance, {@link GeonetworkHealthCheckServlet} will use it
 * instead of {@link HealthChecks}.
 */
public class GeonetworkHealthCheckServlet extends HttpServlet {
    /**
     * The attribute name of the {@link HealthCheckRegistry} instance in the servlet context.
     */
    public static final String REGISTRY_ATTRIBUTE_KEY = "REGISTRY_ATTRIBUTE_KEY";
    private static final long serialVersionUID = 5367584489771010404L;
    private static final String CONTENT_TYPE = "application/json";

    private transient HealthCheckRegistry registry;

    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String registryAttribute = config.getInitParameter(REGISTRY_ATTRIBUTE_KEY);
        registry = (HealthCheckRegistry) config.getServletContext().getAttribute(registryAttribute);
        if (registry == null) {
            throw new IllegalStateException("Expected a HealthCheckRegistery to be registered in the ServletContext attributes but there was none");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();
        resp.setContentType(CONTENT_TYPE);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        final PrintWriter writer = resp.getWriter();
        Element report = new Element("report");
        if (results.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            report.addContent(new Element("msg").setText("No health checks registered."));
        } else {
            if (isAllHealthy(results)) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
                Element healthcheck = new Element("healthcheck");
                final HealthCheck.Result result = entry.getValue();
                healthcheck.addContent(new Element("name").setText(entry.getKey()));
                if (result.isHealthy()) {
                    healthcheck.addContent(new Element("status").setText("OK"));
                    if (result.getMessage() != null) {
                        healthcheck.addContent(new Element("msg").setText(result.getMessage()));
                    }
                } else {
                    healthcheck.addContent(new Element("status").setText("ERROR"));
                    if (result.getMessage() != null) {
                        healthcheck.addContent(new Element("msg").setText(result.getMessage()));
                    }
                    final Throwable error = result.getError();
                    if (error != null) {
                        healthcheck.addContent(new Element("exception").setText(ExceptionUtils.getStackTrace(error)));
                    }
                }
                report.addContent(healthcheck);
            }
            writer.println(Xml.getJSON(report));
        }
        writer.close();
    }
}
