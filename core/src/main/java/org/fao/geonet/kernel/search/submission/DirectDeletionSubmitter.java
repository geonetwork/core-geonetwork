package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DirectDeletionSubmitter implements IDeletionSubmitter {
    public static final DirectDeletionSubmitter INSTANCE = new DirectDeletionSubmitter();

    private DirectDeletionSubmitter() {}

    @Override
    public void submitUUIDToIndex(String uuid, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();
        List<String> documents = Collections.singletonList(uuid);

        BulkRequest bulkRequest = restClient.buildDeleteBulkRequest(searchManager.getDefaultIndex(), documents);
        final BulkResponse bulkItemResponses = restClient.getClient().bulk(bulkRequest);

        searchManager.handleDeletionResponse(bulkItemResponses, documents);
    }

    @Override
    public void submitQueryToIndex(String query, EsSearchManager searchManager) throws IOException {
        EsRestClient restClient = searchManager.getClient();

        DeleteByQueryRequest deleteByQueryRequest = restClient.buildDeleteByQuery(searchManager.getDefaultIndex(), query);
        final DeleteByQueryResponse deleteByQueryResponse = restClient.getClient().deleteByQuery(deleteByQueryRequest);

        searchManager.handleDeletionResponse(deleteByQueryResponse, query);
    }
}
