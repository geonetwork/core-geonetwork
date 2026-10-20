/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package jeeves.server.resources;

import jeeves.server.context.ServiceContext;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate statistics of a connection.
 *
 * User: jeichar Date: 4/5/12 Time: 10:15 AM
 */
public class Stats {
    public final Integer numActive;
    public final Integer numIdle;
    public final Integer maxActive;
    public final Map<String, String> resourceSpecificStats;


    /**
     * Pool stats are read by reflection so that any pool works, including the ones provided by the
     * container through JNDI: commons-dbcp / dbcp2 (Jetty), Tomcat dbcp and jdbc-pool, HikariCP.
     * Values are -1 when the pool doesn't expose them. A negative maxActive can also mean an
     * unbounded pool (dbcp / dbcp2), so use numActive to check if stats are available.
     */
    public Stats(final ServiceContext context) {
        this(context.getBean(DataSource.class));
    }

    public Stats(final DataSource source) {
        Object hikariPool = invoke(source, "getHikariPoolMXBean");
        if (hikariPool != null) {
            numActive = toInt(invoke(hikariPool, "getActiveConnections"));
            numIdle = toInt(invoke(hikariPool, "getIdleConnections"));
            maxActive = toInt(invoke(source, "getMaximumPoolSize"));
        } else {
            numActive = toInt(invoke(source, "getNumActive"));
            numIdle = toInt(invoke(source, "getNumIdle"));
            // dbcp2 / Tomcat dbcp use maxTotal, commons-dbcp 1.x / Tomcat jdbc-pool use maxActive
            Object maxTotal = invoke(source, "getMaxTotal");
            maxActive = toInt(maxTotal != null ? maxTotal : invoke(source, "getMaxActive"));
        }
        resourceSpecificStats = new HashMap<>();
    }

    private static Object invoke(Object target, String method) {
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static int toInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : -1;
    }
}
