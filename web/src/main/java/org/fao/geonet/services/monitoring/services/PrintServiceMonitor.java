//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.monitoring.services;

import jeeves.exceptions.BadXmlResponseEx;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.kernel.reusable.Utils;
import org.fao.geonet.util.Chrono;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Service monitor for print service. Checks access to print service.
 *
 * Report format:
 *
 * <printService>
 *   <status>ok</status>
 *   <url>PRINTSERVICE_URL</status>
 *   <responseTime>MILLIS</responseTime>
 * </printService>
 *
 * <printService>
 *   <status>error</status>
 *   <url>PRINTSERVICE_URL</status>
 *   <errorCode>500|Print service HTTP status code</errorCode>
 *   <errorDescription>EXCEPTION_MESSAGE</errorDescription>
 * </printService>
 *
 */
public class PrintServiceMonitor extends ServiceMonitor {
    private HttpClient client = new HttpClient();
	private HostConfiguration config = new HostConfiguration();

    public void exec(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        String url = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager()) + "/print/info.json";
        Log.debug(Geocat.Module.MONITORING, "Print service url: " + url);

        JSONRequest request = null;

        try {
            request = new JSONRequest(url);
            setupProxy(context, request);

            timeMeasurer = new Chrono();
            JSONObject response = request.execute();
            Log.debug(Geocat.Module.MONITORING, "Print service response: " + response.toString());
                        
            int statusCode = request.getStatusCode();
            Log.debug(Geocat.Module.MONITORING, "Print service status code: " + statusCode);

            if (statusCode ==HttpStatus.SC_OK) {
                updateReport(report, url, timeMeasurer.getMillis());

            } else {
                updateReportError(report, url, statusCode);

                throw new ServiceMonitorException("PrintServiceMonitor (" + url + "): " + HttpStatus.getStatusText(statusCode), statusCode);
            }

        } catch (IOException ex) {
            updateReportError(report, url, ex);

            throw new ServiceMonitorException("PrintServiceMonitor (" + url + "): " + ex.getMessage());

        } catch (BadXmlResponseEx ex) {
            updateReportError(report, url, ex);

            throw new ServiceMonitorException("PrintServiceMonitor (" + url + "): " + ex.getMessage(), ex.getCode());

        } finally {
            timeMeasurer = null;
            request = null;

        }
    }

    private void updateReport(ServiceMonitorReport report, String url, float responseTime) {
        report.addField(ServiceMonitorManager.PRINT_MONITOR_ID, "url", url);
        report.addStatusOk(ServiceMonitorManager.PRINT_MONITOR_ID, responseTime);
    }

    private void updateReportError(ServiceMonitorReport report,  String url, int statusCode) {
        report.addField(ServiceMonitorManager.PRINT_MONITOR_ID, "url", url);
        report.addStatusError(ServiceMonitorManager.PRINT_MONITOR_ID, statusCode + "",
                        "PrintServiceMonitor (" + url + "): " + HttpStatus.getStatusText(statusCode));
    }

    private void updateReportError(ServiceMonitorReport report, String url, Exception ex) {
        report.addField(ServiceMonitorManager.PRINT_MONITOR_ID, "url", url);

        if (ex instanceof BadXmlResponseEx) {
            report.addStatusError(ServiceMonitorManager.PRINT_MONITOR_ID, ((BadXmlResponseEx) ex).getCode() + "",
                             "PrintServiceMonitor (" + url + "): " + ex.getMessage());

        } else {
            report.addStatusError(ServiceMonitorManager.PRINT_MONITOR_ID, "500",
                             "PrintServiceMonitor (" + url + "): " + ex.getMessage());

        }
    }

    private void setupProxy(ServiceContext context, JSONRequest req)
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		boolean enabled = sm.getValueAsBool("system/proxy/use", false);

		if (!enabled)
		{
			req.setUseProxy(false);
		} else
		{
			String host = sm.getValue("system/proxy/host");
			String port = sm.getValue("system/proxy/port");
			String username = sm.getValue("system/proxy/username");
			String password = sm.getValue("system/proxy/password");

			if (!Lib.type.isInteger(port))
			{
				Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : " + port);
			} else
			{
				req.setUseProxy(true);
				req.setProxyHost(host);
				req.setProxyPort(Integer.parseInt(port));
				req.setProxyCredentials(username, password);
			}
		}
	}
}
