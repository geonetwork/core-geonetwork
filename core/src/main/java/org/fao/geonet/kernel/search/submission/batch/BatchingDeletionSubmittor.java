package org.fao.geonet.kernel.search.submission.batch;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.submission.IDeletionSubmittor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BatchingDeletionSubmittor extends BatchingSubmittorBase<BatchingDeletionSubmittor.State> implements IDeletionSubmittor {

    protected static class State extends StateBase {
        private final List<String> listOfIDsToDelete = new ArrayList<>();

        @Override
        protected void cleanUp() {
            // Send any remaining pending documents
            if (!listOfIDsToDelete.isEmpty()) {
                deleteDocumentsFromIndex(listOfIDsToDelete);
            }
        }

        private void deleteDocumentsFromIndex(List<String> toDelete) {
            EsRestClient restClient = searchManager.getClient();
            BulkRequest bulkRequest = restClient.buildDeleteBulkRequest(searchManager.getDefaultIndex(), toDelete);
            CompletableFuture<Void> currentIndexFuture = restClient.getAsyncClient().bulk(bulkRequest)
                .thenAcceptAsync(bulkItemResponses -> {
                    searchManager.handleDeletionResponse(bulkItemResponses, toDelete);
                });
            queueFuture(currentIndexFuture);
        }
    }

    public BatchingDeletionSubmittor() {
        super(new State());
    }

    public BatchingDeletionSubmittor(int estimatedTotalSize) {
        super(new State(), estimatedTotalSize);
    }

    @Override
    public void submitToIndex(String id, EsSearchManager searchManager) throws IOException {
        if (state.closed) {
            throw new IllegalStateException("Attempting to use a closed " + this.getClass().getSimpleName());
        }

        state.searchManager = searchManager;
        List<String> listOfIDsToDelete = state.listOfIDsToDelete;
        listOfIDsToDelete.add(id);
        if (listOfIDsToDelete.size() >= commitInterval) {
            List<String> toDelete = new ArrayList<>(listOfIDsToDelete);
            listOfIDsToDelete.clear();
            state.deleteDocumentsFromIndex(toDelete);
        }
    }
}
