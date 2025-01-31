package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * An index submitter that directly and synchronously transmits new documents to the index
 */
public class DirectIndexSubmitter implements IIndexSubmitter {
    public static final DirectIndexSubmitter INSTANCE = new DirectIndexSubmitter();

    private DirectIndexSubmitter() {}

    @Override
    public void submitToIndex(String id, String jsonDocument, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        Map<String, String> documents = Collections.singletonMap(id, jsonDocument);

        BulkRequest bulkRequest = restClient.buildIndexBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleIndexResponse(bulkItemResponses, documents);
    }
}
