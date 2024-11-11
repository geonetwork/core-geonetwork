package org.fao.geonet.kernel.search.submission.batch;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.submission.IDeletionSubmittor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BatchingDeletionSubmittor extends BatchingSubmittorBase<BatchingDeletionSubmittor.State> implements IDeletionSubmittor {

    protected static class State extends StateBase {
        private final List<String> listOfUUIDsToDelete = new ArrayList<>();

        @Override
        protected void cleanUp() {
            // Send any remaining pending documents
            if (!listOfUUIDsToDelete.isEmpty()) {
                deleteDocumentsFromIndex(listOfUUIDsToDelete);
            }
        }

        private void deleteDocumentsFromIndex(List<String> toDelete) {
            EsRestClient restClient = searchManager.getClient();
            BulkRequest bulkRequest = restClient.buildDeleteBulkRequest(searchManager.getDefaultIndex(), toDelete);
            CompletableFuture<Void> currentIndexFuture = restClient.getAsyncClient().bulk(bulkRequest)
                .thenAcceptAsync(bulkItemResponses -> searchManager.handleDeletionResponse(bulkItemResponses, toDelete));
            queueFuture(currentIndexFuture);
        }

        private void deleteDocumentByQuery(String query) {
            EsRestClient restClient = searchManager.getClient();

            DeleteByQueryRequest deleteByQueryRequest = restClient.buildDeleteByQuery(searchManager.getDefaultIndex(), query);
            final CompletableFuture<Void> deleteByQueryFuture = restClient.getAsyncClient().deleteByQuery(deleteByQueryRequest)
                .thenAcceptAsync(response -> searchManager.handleDeletionResponse(response, query));
            queueFuture(deleteByQueryFuture);
        }
    }

    public BatchingDeletionSubmittor() {
        super(new State());
    }

    public BatchingDeletionSubmittor(int estimatedTotalSize) {
        super(new State(), estimatedTotalSize);
    }

    @Override
    public void submitUUIDToIndex(String uuid, EsSearchManager searchManager) {
        if (state.closed) {
            throw new IllegalStateException("Attempting to use a closed " + this.getClass().getSimpleName());
        }

        state.searchManager = searchManager;
        List<String> listOfUUIDsToDelete = state.listOfUUIDsToDelete;
        listOfUUIDsToDelete.add(uuid);
        if (listOfUUIDsToDelete.size() >= commitInterval) {
            List<String> toDelete = new ArrayList<>(listOfUUIDsToDelete);
            listOfUUIDsToDelete.clear();
            state.deleteDocumentsFromIndex(toDelete);
        }
    }

    @Override
    public void submitQueryToIndex(String query, EsSearchManager searchManager) {
        if (state.closed) {
            throw new IllegalStateException("Attempting to use a closed " + this.getClass().getSimpleName());
        }

        state.searchManager = searchManager;
        state.deleteDocumentByQuery(query);
    }
}
