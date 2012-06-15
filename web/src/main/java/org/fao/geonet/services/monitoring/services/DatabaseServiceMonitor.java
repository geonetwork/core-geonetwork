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

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.Chrono;
import org.jdom.Element;

import java.util.List;

/**
 * Service to monitor database connection. Checks the number of records in metadata table.
 *
 * Report format:
 *
 * <db>
 *   <status>ok</status>
 *   <responseTime>MILLIS</responseTime>
 *   <metadataCount>COUNT</metadataCount>
 * </db>
 *
 * <db>
 *   <status>error</status>
 *   <errorCode>500</errorCode>
 *   <errorDescription>DB_ERROR_CODE + DB_ERROR_DESCRIPTION</errorDescription>
 * </db>
 */
public class DatabaseServiceMonitor extends ServiceMonitor {
    public void exec(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        try {
            timeMeasurer = new Chrono();
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            // Count metadata records
            List list = dbms.select("SELECT count(*) as numr FROM METADATA where isTemplate = 'n'").getChildren();

            updateReport(report, ((Element) list.get(0)).getChildText("numr"), timeMeasurer.getMillis());

        } catch (java.sql.SQLException ex) {
            updateReportError(report, ex);
            
            throw new ServiceMonitorException("DatabaseServiceMonitor: " + ex.getErrorCode() + " " + ex.getMessage());

        } catch (Exception ex) {
             updateReportError(report, ex);
            
            throw new ServiceMonitorException("DatabaseServiceMonitor: " + " " + ex.getMessage());

        } finally {
            timeMeasurer = null;
          
        }
    }

    private void updateReport(ServiceMonitorReport report, String numMetadata, float responseTime) {
        report.addField(ServiceMonitorManager.DB_MONITOR_ID, "metadataCount", numMetadata);
        report.addStatusOk(ServiceMonitorManager.DB_MONITOR_ID, responseTime);
    }

    private void updateReportError(ServiceMonitorReport report, Exception ex) {
        if (ex instanceof java.sql.SQLException) {
            report.addStatusError(ServiceMonitorManager.DB_MONITOR_ID, "500",
                    "DatabaseServiceMonitor: " + ((java.sql.SQLException) ex).getErrorCode() + " " + ex.getMessage());

        } else {
            report.addStatusError(ServiceMonitorManager.DB_MONITOR_ID, "500",
                    "DatabaseServiceMonitor: " + " " + ex.getMessage());
        }
    }
}
