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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

/**
 * Service monitor for indexing
 *
 * Checks the status of indexing service.
 *
 * Report format:
 *
 * <indexService>
 *   <status>indexing|idle</status>
 * </indexService>
 *
 * <indexService>
 *   <status>error</status>
 *   <errorCode>500</errorCode>
 *   <errorDescription>EXCEPTION_MESSAGE</errorDescription>
 * </indexService>
 */
public class IndexServiceMonitor extends ServiceMonitor implements Service {

    public void exec(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        try {
            // TODO check indexing
            if( gc.getDataManager().isIndexing() ){

                updateReport(report, "indexing");
            } else {

                updateReport(report, "idle");
            }
            updateReport(report, "idle");

        } catch (Exception ex) {
            updateReportError(report, ex);
            
            throw new ServiceMonitorException( "IndexServiceMonitor: " + ex.getMessage());
        }
    }

    private void updateReport(ServiceMonitorReport report, String status) {
        report.addField(ServiceMonitorManager.INDEXER_MONITOR_ID, "status", status);
    }

    private void updateReportError(ServiceMonitorReport report, Exception ex) {
        report.addStatusError(ServiceMonitorManager.INDEXER_MONITOR_ID, "500", "IndexServiceMonitor: " + ex.getMessage());
    }

	public void init(String appPath, ServiceConfig params) throws Exception {}

	public Element exec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

	    return new Element("IndexReport").setText(""+gc.getDataManager().isIndexing());
    }
}
