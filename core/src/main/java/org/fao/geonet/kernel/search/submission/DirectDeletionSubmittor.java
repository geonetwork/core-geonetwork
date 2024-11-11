package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DirectDeletionSubmittor implements IDeletionSubmittor {
    public static final DirectDeletionSubmittor INSTANCE = new DirectDeletionSubmittor();

    private DirectDeletionSubmittor() {}

    @Override
    public void submitToIndex(String uuid, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        List<String> documents = Collections.singletonList(uuid);

        BulkRequest bulkRequest = restClient.buildDeleteBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleDeletionResponse(bulkItemResponses, documents);
    }
}
