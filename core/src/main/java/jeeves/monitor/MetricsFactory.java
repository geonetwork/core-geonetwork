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

package jeeves.monitor;

import com.yammer.metrics.core.MetricsRegistry;

import jeeves.server.context.ServiceContext;

/**
 * Class for creating metrics objects (http://metrics.codahale.com/) which only require a
 * ServiceContext object for performing the check. The health check object will be created and added
 * after AppHandler is created and started.
 *
 * It will create HealthCheck or Gauge objects most likely and will be a plugin type model.  In the
 * config.xml there should be a section
 *
 * <monitors></monitors>
 *
 * That declares what factories should be created.  See config-monitoring.xml for examples and
 * documentation.
 *
 * User: jeichar Date: 3/29/12 Time: 3:29 PM
 */
public interface MetricsFactory<Type> {
    /**
     * Create a metrics object of type Type
     */
    Type create(MetricsRegistry metricsRegistry, ServiceContext context);
}
