package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.repository.statistic.SearchRequestRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Verify that SearchLogger has appropriate transaction as needed.
 *
 * Created by Jesse on 3/11/14.
 */
public class SearchLoggerTaskIntegerTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SearchRequestRepository _searchRequestRepository;

    @Test
    public void testRun() throws Exception {
        final long numSearches = _searchRequestRepository.count();
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final ServiceContext context = createServiceContext();
                final SearchLoggerTask task = context.getBean(SearchLoggerTask.class);
                Query query = new TermQuery(new Term("any", "search"));
                task.configure(context, false, "any", query, 3, Sort.RELEVANCE, null, "value");
                task.run();
            }
        });
        assertEquals(numSearches+1, _searchRequestRepository.count());
    }
}
