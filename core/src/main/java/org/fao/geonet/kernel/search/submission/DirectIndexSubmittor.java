package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * An index submittor that directly and synchronously transmits new documents to the index
 */
public class DirectIndexSubmittor implements IIndexSubmittor {
    public static final DirectIndexSubmittor INSTANCE = new DirectIndexSubmittor();

    private DirectIndexSubmittor() {}

    @Override
    public void submitToIndex(String id, String jsonDocument, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        Map<String, String> documents = Collections.singletonMap(id, jsonDocument);

        BulkRequest bulkRequest = restClient.buildBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleIndexResponse(bulkItemResponses, documents);
    }
}
