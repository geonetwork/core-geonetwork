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

package jeeves.server.resources;

import jeeves.server.context.ServiceContext;

import org.apache.commons.dbcp2.BasicDataSource;

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


    public Stats(final ServiceContext context) {
        DataSource source = context.getBean(DataSource.class);
        if (source instanceof BasicDataSource) {
            BasicDataSource basicDataSource = (BasicDataSource) source;
            numActive = basicDataSource.getNumActive();
            numIdle = basicDataSource.getNumIdle();
            maxActive = basicDataSource.getMaxTotal();
        } else {
            maxActive = numActive = numIdle = -1;
        }
        resourceSpecificStats = new HashMap<String, String>();
    }
}
