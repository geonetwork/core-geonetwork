package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

import com.yammer.metrics.core.HealthCheck;

/**
 * Verifies that all metadata have been correctly indexed (without errors)
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class NoIndexErrorsHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Metadata Index Errors") {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SearchManager searchMan = gc.getSearchmanager();

                IndexAndTaxonomy indexAndTaxonomy= searchMan.getNewIndexReader(null);
                GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
                try {
                    TermQuery indexError = new TermQuery(new Term("_indexingError", "1"));
                    TopDocs hits = new IndexSearcher(reader).search(indexError, 1);
                    if (hits.totalHits > 0) {
                        return Result.unhealthy("Found "+hits.totalHits+" metadata that had errors during indexing");
                    } else {
                        return Result.healthy();
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
