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

package org.fao.geonet.services.monitoring;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.monitoring.services.ServiceMonitorReport;
import org.jdom.Element;

public class Check implements Service {
    private ServiceConfig _config;

	public void init(String appPath, ServiceConfig config) throws Exception {
		_config = config;
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {

        ServiceMonitorReport report = new ServiceMonitorReport();
        
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        // Monitor services launch ServiceMonitorException if any error
        gc.getServiceMonitorManager().checkServicesStatus(context, report);

        // Services ok, return report
        Element result = report.buildXmlReport();

        Log.debug(Geocat.Module.MONITORING, "Monitoring report");
        Log.debug(Geocat.Module.MONITORING, Xml.getString(result));

        return result;       
	}
}
