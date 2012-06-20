//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the

package org.fao.geonet.guiservices.monitoring;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.monitoring.services.ServiceMonitorReport;
import org.jdom.Element;
/**
 * Service to get a report of the status for the monitored application services
 */
public class Get implements Service {

	public void init(String appPath, ServiceConfig config) throws Exception {}

	public Element exec(Element params, ServiceContext context)
			throws Exception {

        ServiceMonitorReport report = new ServiceMonitorReport();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        gc.getServiceMonitorManager().createServicesStatusReport(context, report) ;

        Element result = report.buildXmlReport();
        
        Log.debug(Geonet.MONITORING, "Monitoring report");
        Log.debug(Geonet.MONITORING, Xml.getString(result));

        return result;
	}
}
