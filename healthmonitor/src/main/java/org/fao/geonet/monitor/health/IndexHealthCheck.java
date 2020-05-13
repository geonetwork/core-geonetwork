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

import com.yammer.metrics.core.HealthCheck;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.springframework.beans.factory.BeanCreationException;

/**
 * Checks to ensure that the Elasticsearch index is up and running.
 */
public class IndexHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                try {
                    EsSearchManager searchMan = gc.getBean(EsSearchManager.class);

                    try {
                        Search search = new Search.Builder("")
                            .addIndex(searchMan.getIndex())
                            .addType(searchMan.getIndexType())
                            .build();
                        final SearchResult result = searchMan.getClient().getClient().execute(search);

                        if (result.isSucceeded()) {
                            return Result.healthy(String.format(
                                "%s records indexed in remote index currently.",
                                result.getHits(Object.class).size()
                            ));
                        } else {
                            return Result.unhealthy(
                                "Index storing records is not available currently. " +
                                    "This component is only required if you use WFS features indexing " +
                                    "and dashboards.");
                        }
                    } catch (Throwable e) {
                        return Result.unhealthy(e);
                    }

                } catch (BeanCreationException e) {
                    return Result.unhealthy("Remote index module is not installed in your catalogue " +
                        "installation. Add 'es' to the spring.profiles.active in WEB-INF/web.xml to " +
                        "activate it.");
                }
            }
        };
    }
}
