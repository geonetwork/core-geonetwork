package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.SearchManager;

import com.yammer.metrics.core.HealthCheck;

/**
 * Checks to ensure that only iso19139.che metadata are in the database
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class MetadataTypeHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("iso19139.che only Metadata") {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SearchManager searchMan = gc.getSearchmanager();

                IndexReader reader = searchMan.getIndexReader(null);
                try {
                    BooleanQuery query = new BooleanQuery();
                    TermQuery schemaIsIso19139CHE = new TermQuery(new Term("_schema", "iso19139.che"));
                    TermQuery notHarvested = new TermQuery(new Term("_isHarvested", "n"));
                    query.add(new BooleanClause(notHarvested, Occur.MUST));
                    query.add(new BooleanClause(schemaIsIso19139CHE, Occur.MUST_NOT));
                    TopDocs hits = new IndexSearcher(reader).search(query, 1);
                    if (hits.totalHits > 0) {
                        return Result.unhealthy("Found "+hits.totalHits+" metadata that were not harvested but do not have iso19139.che schema");
                    } else {
                        return Result.healthy();
                    }
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                } finally {
                    searchMan.releaseIndexReader(reader);
                }
            }
        };
    }
}
