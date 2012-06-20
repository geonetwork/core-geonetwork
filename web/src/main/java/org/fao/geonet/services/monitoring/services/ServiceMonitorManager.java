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
import jeeves.utils.Log;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;

import java.util.HashMap;

/**
 * Class to manage service monitoring services
 *
 */
public class ServiceMonitorManager {
    public final static String DB_MONITOR_ID = "db";
    public final static String PRINT_MONITOR_ID = "printService";
    public final static String CSW_MONITOR_ID = "cswService";
    public final static String INDEXER_MONITOR_ID = "indexService";
    public final static String FREEDISK_MONITOR_ID = "freediskService";
    
    private HashMap<String, ServiceMonitor> monitorServices;

    public ServiceMonitorManager(Dbms dbms, ServiceContext context, SettingManager sm) {
        Log.info(Geocat.Module.MONITORING, "Service monitoring init");

        // Add services to monitor
        monitorServices = new HashMap<String, ServiceMonitor>();

        addService(DB_MONITOR_ID, new DatabaseServiceMonitor());
        Log.info(Geocat.Module.MONITORING, "Added database monitor");
//		PMT geocat2 c2c : disabled for now
//        addService(PRINT_MONITOR_ID, new PrintServiceMonitor(context));
//        Log.info(Geocat.Module.MONITORING, "Added print service monitor");

        addService(CSW_MONITOR_ID, new CswServiceMonitor());
        Log.info(Geocat.Module.MONITORING, "Added CSW service monitor");

        addService(INDEXER_MONITOR_ID, new IndexServiceMonitor());
        Log.info(Geocat.Module.MONITORING, "Added indexer service monitor");

        addService(FREEDISK_MONITOR_ID, new FreeDiskServiceMonitor());
        Log.info(Geocat.Module.MONITORING, "Added free disk service monitor");
    }

    /**
     * Adds a service to the ServiceMonitorManager
     *
     * @param serviceId             ServiceMonitor identifier
     * @param serviceMonitor        Class that implements the ServiceMonitorManager
     */
    public void addService(String serviceId, ServiceMonitor serviceMonitor) {
        monitorServices.put(serviceId, serviceMonitor);
    }

    /**
     * Creates a report status of all monitored services
     *
     * @param context
     * @param report
     */
    public void createServicesStatusReport(ServiceContext context, ServiceMonitorReport report) {
        for(ServiceMonitor servMonitor: monitorServices.values()) {
            try {
                servMonitor.exec(context, report);
            } catch (ServiceMonitorException ex) {
                // Only log exceptions, report contains status information
                Log.error(Geocat.Module.MONITORING, "Error code: "+ ex.getCode() + ", Exception:" + ex.getMessage());
            }
        }
    }

    /**
     * Cheacks the status of monitored services. Throws ServiceMonitorException if a service fails
     *
     * @param context
     * @param report
     */
    public void checkServicesStatus(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        for(ServiceMonitor servMonitor: monitorServices.values()) {
            servMonitor.exec(context, report);
        }
    }
}
