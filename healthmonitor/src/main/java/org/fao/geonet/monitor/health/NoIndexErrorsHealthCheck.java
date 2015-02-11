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

                SearchManager searchMan = gc.getBean(SearchManager.class);
                ServiceConfig config = new ServiceConfig();
                config.setValue(Geonet.SearchResult.RESULT_TYPE, "hits");
                final MetaSearcher metaSearcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
                Element request = new Element("request")
                        .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
                        .addContent(new Element(SearchManager.INDEXING_ERROR_FIELD).setText("1"))
                        .addContent(new Element("from").setText("1"))
                        .addContent(new Element("to").setText("50"));
                metaSearcher.search(context, request, config);

                if (metaSearcher.getSize() > 0) {
                    return Result.unhealthy("Found "+metaSearcher.getSize()+" metadata that had errors during indexing");
                } else {
                    return Result.healthy();
                }
            }
        };
    }
}
