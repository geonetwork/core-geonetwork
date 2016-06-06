/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package jeeves.monitor.healthcheck;

import com.sun.management.UnixOperatingSystemMXBean;
import com.yammer.metrics.core.HealthCheck;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Checks the number of free and used file handles and ensures that 1% are free.
 *
 * Only works on unix-based systems.  On windows it always returns a healthy result
 *
 * @author jeichar
 */
public class FreeFileHandlesHealthCheck implements HealthCheckFactory {

    @Override
    public HealthCheck create(ServiceContext context) {
        return new HealthCheck("Free file handles") {
            @Override
            protected Result check() throws Exception {
                try {
                    OperatingSystemMXBean osMbean = ManagementFactory.getOperatingSystemMXBean();
                    if (osMbean instanceof UnixOperatingSystemMXBean) {
                        UnixOperatingSystemMXBean unixMXBean = (UnixOperatingSystemMXBean) osMbean;
                        long free = unixMXBean.getMaxFileDescriptorCount() - unixMXBean.getOpenFileDescriptorCount();
                        double fivePercent = Math.max(2.0, ((double) unixMXBean.getMaxFileDescriptorCount()) * 0.01);
                        if (free < fivePercent) {
                            return Result.unhealthy("There are insufficient free file handles. Connections free:" + free);
                        }
                    }
                    return Result.healthy();
                } catch (Exception e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }

}
