package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.index.IndexReader;
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
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class LuceneIndexHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Lucene Index") {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SearchManager searchMan = gc.getSearchmanager();


                IndexAndTaxonomy indexAndTaxonomy= searchMan.getNewIndexReader(null);
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
