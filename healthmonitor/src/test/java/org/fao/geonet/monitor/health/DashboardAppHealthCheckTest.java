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

package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;

import org.apache.http.client.methods.HttpUriRequest;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.junit.Test;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DashboardAppHealthCheckTest {

    private HealthCheck.Result runCheck(String dashboardAppUrl) throws Exception {
        return runCheck(dashboardAppUrl, mock(GeonetHttpRequestFactory.class));
    }

    private HealthCheck.Result runCheck(String dashboardAppUrl, GeonetHttpRequestFactory httpRequestFactory)
        throws Exception {
        ServiceContext context = mock(ServiceContext.class);
        EsSearchManager searchManager = mock(EsSearchManager.class);
        EsRestClient esRestClient = mock(EsRestClient.class);

        when(context.getBean(GeonetHttpRequestFactory.class)).thenReturn(httpRequestFactory);
        when(context.getBean(EsSearchManager.class)).thenReturn(searchManager);
        when(searchManager.getClient()).thenReturn(esRestClient);
        when(esRestClient.getDashboardAppUrl()).thenReturn(dashboardAppUrl);

        return new DashboardAppHealthCheck().create(context).execute();
    }

    @Test
    public void testKibanaNotConfigured_ReturnsHealthyDisabledResult() throws Exception {
        HealthCheck.Result result = runCheck("");

        assertTrue("A disabled Kibana check must not fail the overall health check", result.isHealthy());
        assertTrue(HealthCheckFactory.isDisabled(result));
        assertEquals("Kibana is currently not enabled.", HealthCheckFactory.getDisabledMessage(result));
    }

    @Test
    public void testKibanaUrlBlank_ReturnsHealthyDisabledResult() throws Exception {
        HealthCheck.Result result = runCheck(null);

        assertTrue(result.isHealthy());
        assertTrue(HealthCheckFactory.isDisabled(result));
    }

    @Test
    public void testKibanaConfiguredAndReachable_ReturnsHealthyRunningResult() throws Exception {
        GeonetHttpRequestFactory httpRequestFactory = mock(GeonetHttpRequestFactory.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getRawStatusCode()).thenReturn(200);
        when(httpRequestFactory.execute(any(HttpUriRequest.class))).thenReturn(response);

        HealthCheck.Result result = runCheck("http://localhost:5601", httpRequestFactory);

        assertTrue(result.isHealthy());
        assertFalse(HealthCheckFactory.isDisabled(result));
    }

    @Test
    public void testKibanaConfiguredWithCustomBasePath_ReturnsHealthyRunningResult() throws Exception {
        GeonetHttpRequestFactory httpRequestFactory = mock(GeonetHttpRequestFactory.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getRawStatusCode()).thenReturn(404);
        when(httpRequestFactory.execute(any(HttpUriRequest.class))).thenReturn(response);

        HealthCheck.Result result = runCheck("http://localhost:5601", httpRequestFactory);

        assertTrue("A 404 may just mean Kibana uses a custom basePath and is still considered healthy",
            result.isHealthy());
    }

    @Test
    public void testKibanaConfiguredButUnreachable_ReturnsUnhealthyResult() throws Exception {
        GeonetHttpRequestFactory httpRequestFactory = mock(GeonetHttpRequestFactory.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getRawStatusCode()).thenReturn(503);
        when(httpRequestFactory.execute(any(HttpUriRequest.class))).thenReturn(response);

        HealthCheck.Result result = runCheck("http://localhost:5601", httpRequestFactory);

        assertFalse(result.isHealthy());
    }

    @Test
    public void testKibanaConfiguredThrowsException_ReturnsUnhealthyResult() throws Exception {
        GeonetHttpRequestFactory httpRequestFactory = mock(GeonetHttpRequestFactory.class);
        when(httpRequestFactory.execute(any(HttpUriRequest.class))).thenThrow(new IOException("connection refused"));

        HealthCheck.Result result = runCheck("http://localhost:5601", httpRequestFactory);

        assertFalse(result.isHealthy());
    }
}
