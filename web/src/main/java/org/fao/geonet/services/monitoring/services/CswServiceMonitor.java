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

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.utils.XmlRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.exceptions.CatalogException;

import org.fao.geonet.lib.Lib;
import org.fao.geonet.kernel.reusable.Utils;
import org.fao.geonet.util.Chrono;
import org.jdom.Element;

import java.net.URL;

/**
 * Service monitor for CSW service. Checks GetCapabilities and GetRecords requests.
 *
 * Report format:
 *
 * <cswService>
 *   <status>disabled</status>
 * </cswService>
 *
 * <cswService>
 *   <status>enabled</status>
 * </cswService>
 * <cswService_capabilities>
 *   <url>CAPABILITIES_URL</url>
 *   <status>ok</status>
 *   <responseTime>MILLIS</responseTime>
 * </cswService_capabilities>
 * <cswService_getrecords>
 *   <status>ok</status>
 *   <url>GETRECORDS_URL</url>
 *   <responseTime>MILLIS</responseTime>
 * </cswService_getrecords>
 *
 * <cswService>
 *   <status>enabled</status>
 * </cswService>
 * <cswService_capabilities>
 *   <status>error</status>
 *   <url>CAPABILITIES_URL</url>
 *   <errorCode>500</errorCode>
 *   <errorDescription>EXCEPTION_MESSAGE</errorDescription>
 * </cswService_capabilities>
 * 
 */
public class CswServiceMonitor extends ServiceMonitor {

    public void exec(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        boolean cswEnable    = gc.getSettingManager().getValueAsBool("system/csw/enable", false);

        report.addField(ServiceMonitorManager.CSW_MONITOR_ID, "enabled", new Boolean(cswEnable).toString());
        
        if (cswEnable) {
            // Check access to capabilities document
            checkAccessCapabilities(context, report);

            // Check access to GetRecords
            checkAccessGetRecords(context, report);
        }
    }


    private void checkAccessCapabilities(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        String capabUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager()) + "/srv/" +
                           context.getLanguage() + "/csw?request=GetCapabilities&service=CSW&version=2.0.2";

        String reportSection = ServiceMonitorManager.CSW_MONITOR_ID + "_capabilities";

        XmlRequest req = null;

        try {
            req = new XmlRequest(new URL(capabUrl));
            Lib.net.setupProxy(context, req);

            timeMeasurer = new Chrono();
            Element capabil = req.execute();
            Log.debug(Geocat.Module.MONITORING, "CAPABILITIES: " + capabil.getName());
            Log.debug(Geocat.Module.MONITORING, "CAPABILITIES: " + Xml.getString(capabil));

            int statusCode = req.getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                if (capabil.getName().equals("Capabilities")) {
                    updateReport(report, reportSection, capabUrl, timeMeasurer.getMillis());

                } else if (capabil.getName().equals("ExceptionReport")) {
                    CatalogException.unmarshal(capabil);

                }

            } else {
                updateReportError(report, reportSection, capabUrl, statusCode);

                throw new ServiceMonitorException("CSWServiceMonitor (" + capabUrl + "): " + HttpStatus.getStatusText(statusCode), statusCode);
            }

        } catch (ServiceMonitorException se)  {
            throw se;

        } catch (CatalogException ce)  {
            updateReportError(report, reportSection, capabUrl, ce);

            throw new ServiceMonitorException("CSW Service (GetCapabilities): " + ce.toString());

        } catch(Exception ex) {
            updateReportError(report, reportSection, capabUrl, ex);

            throw new ServiceMonitorException("CSWServiceMonitor (GetCapabilities): " + ex.getMessage());

        } finally {
            timeMeasurer = null;
            req = null;
        }
    }


    private void checkAccessGetRecords(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        String params ="request=GetRecords&service=CSW&version=2.0.2&resultType=results&" +
                    "namespace=xmlns%28csw%3Dhttp%3A%2F%2Fwww.opengis.net%2Fcat%2Fcsw%2F2.0.2%29%2Cxmlns%28gmd%3Dhttp%3A%2F%2Fwww.isotc211.org%2F2005%2Fgmd%29&" +
                    "startPosition=1&maxRecords=20&elementSetName=summary&constraintLanguage=CQL_TEXT&" +
                     "constraint_language_version=1.1.0&typeNames=csw%3ARecord";

        String getRecordsUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager()) + "/srv/" +  context.getLanguage() + "/csw?" + params;

        String reportSection = ServiceMonitorManager.CSW_MONITOR_ID + "_getrecords";

        XmlRequest req = null;

        try {
            req = new XmlRequest(new URL(getRecordsUrl));
	        Lib.net.setupProxy(context, req);

            timeMeasurer = new Chrono();
            Element getRecords = req.execute();
            Log.debug(Geocat.Module.MONITORING, "GETRECORDS: " + getRecords.getName());
            Log.debug(Geocat.Module.MONITORING, "GETRECORDS: " + Xml.getString(getRecords));

            int statusCode = req.getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                if (getRecords.getName().equals("GetRecordsResponse")) {
                    updateReport(report, reportSection, getRecordsUrl, timeMeasurer.getMillis());

                } else if (getRecords.getName().equals("ExceptionReport")) {
                    CatalogException.unmarshal(getRecords);

                }
                
            } else {
                updateReportError(report, reportSection, getRecordsUrl, statusCode);

                throw new ServiceMonitorException("CSWServiceMonitor (" + getRecordsUrl + "): " + HttpStatus.getStatusText(statusCode), statusCode);
            }

        } catch (ServiceMonitorException se)  {
            throw se;

        } catch (CatalogException ce)  {
            updateReportError(report, reportSection, getRecordsUrl, ce);

            throw new ServiceMonitorException("CSWServiceMonitor (GetRecords): " + ce.toString());

        } catch(Exception ex) {
            ex.printStackTrace();
            updateReportError(report, reportSection, getRecordsUrl, ex);

            throw new ServiceMonitorException("CSWServiceMonitor (GetRecords): " + ex.getMessage());

        } finally {
            timeMeasurer = null;
            req = null;
            
        }
    }

    private void updateReport(ServiceMonitorReport report, String name, String url, float millis) {
        report.addField(name, "url", url);
        report.addStatusOk(name, millis);
    }

    private void updateReportError(ServiceMonitorReport report, String name, String url, int statusCode) {
        report.addField(name, "url", url);
        report.addStatusError(name, statusCode + "",
                        "CSWServiceMonitor (" + url + "): " + HttpStatus.getStatusText(statusCode));
    }


    private void updateReportError(ServiceMonitorReport report, String name, String url, Exception ex) {
        report.addField(name, "url", url);

        if (ex instanceof CatalogException) {
            report.addStatusError(name, "500",
                         ((CatalogException) ex).toString());

        } else {
            report.addStatusError(name, "500",
                         ex.getMessage());

        }
    }
}
