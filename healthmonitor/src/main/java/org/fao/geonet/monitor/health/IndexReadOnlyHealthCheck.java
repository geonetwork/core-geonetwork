/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

/**
 * Checks to ensure that the Elasticsearch index is not in readonly mode.
 *
 * This can happen when disk space available is lower than 10% (default).
 * Index administrator can force state of the index using:
 * <pre>
 *     curl -X PUT "localhost:9200/gn-records/_settings" \
 *     -H 'Content-Type: application/json' \
 *     -d'{ "index.blocks.read_only_allow_delete" : true } }'
 * </pre>
 */
public class IndexReadOnlyHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() {
                try {
                    ApplicationContext applicationContext = ApplicationContextHolder.get();
                    EsSearchManager esSearchManager = applicationContext.getBean(EsSearchManager.class);

                    String indexName = esSearchManager.getDefaultIndex();
                    boolean isReadOnly = esSearchManager.isIndexWritable(indexName);

                    if (!isReadOnly) {
                        return Result.healthy(String.format(
                            "Index '%s' is writable.",
                            indexName
                        ));
                    } else {
                        return Result.unhealthy(
                            "Index is in Readonly mode. Check disk usage and/or indexing server logs.");
                    }
                } catch (IOException | ElasticsearchException e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
