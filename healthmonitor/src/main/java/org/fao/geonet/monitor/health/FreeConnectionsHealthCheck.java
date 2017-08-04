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

package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.Stats;

import org.fao.geonet.constants.Geonet;

import com.yammer.metrics.core.HealthCheck;

/**
 * Checks that 1% of the connections are free of the main database is free. This is normally a
 * warning health check since if it fails that does not mean the system isn't working but rather
 * that a failure will likely happen soon
 *
 * @author jeichar
 */
public class FreeConnectionsHealthCheck implements HealthCheckFactory {

    @Override
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                Stats stats;
                try {
                    stats = new Stats(context);
                    int free = stats.maxActive - stats.numActive;
                    double fivePercent = Math.max(2.0, ((double) stats.maxActive) * 0.01);
                    if (free < fivePercent) {
                        return Result.unhealthy("There are insufficient free connections on database" + Geonet.Res.MAIN_DB
                            + ".  Connections free:" + free);
                    }
                    return Result.healthy();
                } catch (Exception e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
