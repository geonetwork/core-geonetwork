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

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.jdom.Element;

/**
 * Verifies that all metadata have been correctly indexed (without errors)
 * <p/>
 * User: jeichar Date: 3/26/12 Time: 9:01 AM
 */
public class NoIndexErrorsHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SearchManager searchMan = gc.getBean(SearchManager.class);
                ServiceConfig config = new ServiceConfig();
                config.setValue(Geonet.SearchResult.RESULT_TYPE, "hits");
                try (MetaSearcher metaSearcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
                    Element request = new Element("request")
                        .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
                        .addContent(new Element(SearchManager.INDEXING_ERROR_FIELD).setText("1"))
                        .addContent(new Element("from").setText("1"))
                        .addContent(new Element("to").setText("50"));
                    metaSearcher.search(context, request, config);

                    if (metaSearcher.getSize() > 0) {
                        return Result.unhealthy("Found " + metaSearcher.getSize() + " metadata that had errors during indexing");
                    } else {
                        return Result.healthy();
                    }
                }
            }
        };
    }
}
