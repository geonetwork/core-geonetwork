/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Checks to ensure that the Kibana is up and running.
 */
public class DashboardAppHealthCheck implements HealthCheckFactory {

    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                final GeonetHttpRequestFactory httpRequestFactory = context.getBean(GeonetHttpRequestFactory.class);
                final EsSearchManager searchMan = context.getBean(EsSearchManager.class);
                final String dashboardAppUrl = searchMan.getClient().getDashboardAppUrl();

                if (StringUtils.isNotEmpty(dashboardAppUrl)) {
                    ClientHttpResponse httpResponse = null;
                    try {
                        String url = dashboardAppUrl;
                        httpResponse = httpRequestFactory.execute(new HttpGet(url));

                        if (httpResponse.getRawStatusCode() == 200 // Kibana default config
                            || httpResponse.getRawStatusCode() == 404 // Kibana alive but probably using a custom basePath
                        ) {
                            return Result.healthy(
                                "Dashboard application is running."
                            );
                            // Could make sense to do some more checks.
                            // Kibana status may be red/yellow ?
                            // String url = settingManager.getBaseURL() + "dashboards/api/status";
                            // but proxy need authentication
                        } else {
                            return Result.unhealthy(
                                "Dashboard application is not available currently. " +
                                    "This component is only required if you use dashboards.");
                        }
                    } catch (Exception e) {
                        return Result.unhealthy(e);
                    } finally {
                        if (httpResponse != null) {
                            httpResponse.close();
                        }
                    }
                } else {
                    return Result.unhealthy(
                        "Dashboard application is not configured. " +
                            "Update config.properties to setup Kibana to use this feature.");
                }
            }
        };
    }
}
