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

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.springframework.context.ApplicationContext;

/**
 * Checks to ensure that the Elasticsearch index is up and running.
 */
public class IndexHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() {
                try {
                    ApplicationContext applicationContext = ApplicationContextHolder.get();
                    EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);
                    final SearchResponse result = searchMan.query("*", null, 0, 0);

                    return Result.healthy(String.format(
                        "%s records indexed in remote index currently.",
                        result.hits().total().value()
                    ));
                } catch (Exception e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
