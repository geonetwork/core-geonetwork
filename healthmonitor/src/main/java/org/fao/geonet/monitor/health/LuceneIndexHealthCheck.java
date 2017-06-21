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
import jeeves.server.context.ServiceContext;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

/**
 * Checks to ensure that the database is accessible and readable
 * <p/>
 * User: jeichar Date: 3/26/12 Time: 9:01 AM
 */
public class LuceneIndexHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck(this.getClass().getSimpleName()) {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SearchManager searchMan = gc.getBean(SearchManager.class);


                IndexAndTaxonomy indexAndTaxonomy = searchMan.getNewIndexReader(null);
                GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
                try {
                    Query query = new MatchAllDocsQuery();
                    TopDocs hits = new IndexSearcher(reader).search(query, 1);
                    if (hits.totalHits > 1) {
                        return Result.healthy();
                    } else {
                        return Result.unhealthy("Lucene search for 1 record returned " + hits.totalHits + " hits.");
                    }
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                } finally {
                    searchMan.releaseIndexReader(indexAndTaxonomy);
                }
            }
        };
    }
}
