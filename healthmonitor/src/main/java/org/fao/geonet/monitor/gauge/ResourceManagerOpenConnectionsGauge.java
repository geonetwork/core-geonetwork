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

package org.fao.geonet.monitor.gauge;

import jeeves.server.resources.Stats;

/**
 * Gauge that gets the number of connections that the ResourceProvider reports as open.  If unable
 * to access information or if the number is null (Like in case of JNDI) Integer.MIN_VALUE will be
 * reported
 *
 * User: jeichar Date: 4/5/12 Time: 4:29 PM
 */
public class ResourceManagerOpenConnectionsGauge extends AbstractResourceManagerStatsGauge<Integer> {
    public ResourceManagerOpenConnectionsGauge() {
        super("Open_Connections_By_ResourceProvider");
    }

    @Override
    protected Integer valueImpl(Stats stats) {
        return stats.numActive;
    }

    @Override
    protected Integer defaultValue() {
        return Integer.MIN_VALUE;
    }
}
