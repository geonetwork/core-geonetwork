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
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


/**
 * Service to monitor free disk space. Checks the free disk space in the drive
 * where is installed the application. If the size is less than a minimum then
 * a ServiceMonitorException is launched
 *
 * Report format:
 *
 * <freediskService>
 *   <status>ok</status>
 *   <freeSpace>FREE_SPACE</freeSpace>
 * </freediskService>
 *
 * <db>
 *   <status>error</status>
 *   <freeSpace>FREE_SPACE</freeSpace>
 *   <errorCode>500</errorCode>
 *   <errorDescription>ERROR_DESCRIPTION</errorDescription>
 * </db>
 */
public class FreeDiskServiceMonitor extends ServiceMonitor {
    private final long MINIMUM_FREE_SPACE = 1048576;   // kb (1 Gb)

    public void exec(ServiceContext context, ServiceMonitorReport report) throws ServiceMonitorException {
        try {
            String drive = FilenameUtils.getPrefix(context.getAppPath());
            long kb = FileSystemUtils.freeSpaceKb(drive); 
            String freeSpace = FileUtils.byteCountToDisplaySize(kb*1024);

            if (kb > MINIMUM_FREE_SPACE) {
                updateReport(report, freeSpace);
                
            } else {
                String errorMessage = "FreeDiskMonitor: free space (" + freeSpace + ") lower than " +
                    FileUtils.byteCountToDisplaySize(MINIMUM_FREE_SPACE*1024);

                updateReportError(report, errorMessage, freeSpace);
                
                throw new ServiceMonitorException(errorMessage);
            }

        } catch (ServiceMonitorException se) {
            throw se;

        } catch (Exception ex) {
             updateReportError(report, ex);

            throw new ServiceMonitorException("FreeDiskMonitor: " + " " + ex.getMessage());
        }
    }

    private void updateReport(ServiceMonitorReport report, String freeSpace) {
        report.addField(ServiceMonitorManager.FREEDISK_MONITOR_ID, "freespace", freeSpace);

        report.addStatusOk(ServiceMonitorManager.FREEDISK_MONITOR_ID, 0);
    }

    private void updateReportError(ServiceMonitorReport report, String errorMessage, String freeSpace) {
        report.addField(ServiceMonitorManager.FREEDISK_MONITOR_ID, "freespace", freeSpace);

        report.addStatusError(ServiceMonitorManager.FREEDISK_MONITOR_ID, "500",
                    errorMessage);
    }

    private void updateReportError(ServiceMonitorReport report, Exception ex) {
            report.addStatusError(ServiceMonitorManager.FREEDISK_MONITOR_ID, "500",
                    "FreeDiskMonitor: " + " " + ex.getMessage());
    }
}
