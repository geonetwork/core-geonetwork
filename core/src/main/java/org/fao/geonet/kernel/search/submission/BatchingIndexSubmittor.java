package org.fao.geonet.kernel.search.submission;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An index submitter that batches documents into larger chunks and sends them asynchronously to the index.
 * When closing, this submittor sends the remaining item to the index and waits until all elasticsearch requests have been received,
 * so after closing there are no pending changes
 */
public class BatchingIndexSubmittor implements AutoCloseable, IIndexSubmittor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);
    private static final Cleaner CLEANER = Cleaner.create();
    /**
     * Maximum number of inflight bulk requests before waiting for the Elasticsearch
     */
    private static final int MAX_INFLIGHT_INDEX_REQUESTS = 4;

    private static class State implements Runnable {
        private final Map<String, String> listOfDocumentsToIndex = new HashMap<>();
        @SuppressWarnings("unchecked")
        private final CompletableFuture<Void>[] inflightFutures = new CompletableFuture[MAX_INFLIGHT_INDEX_REQUESTS];
        private int index;
        private EsSearchManager searchManager;
        private boolean closed = false;

        @Override
        public void run() {
            if (!closed) {
                LOGGER.error("BatchingIndexSubmittor was not closed before it was cleaned! Sending any remaining data");
            }
            // Send any remaining pending documents
            if (!listOfDocumentsToIndex.isEmpty()) {
                sendDocumentsToIndex(listOfDocumentsToIndex);
            }
        }

        private void sendDocumentsToIndex(Map<String, String> toIndex) {
            EsRestClient restClient = searchManager.getClient();
            BulkRequest bulkRequest = restClient.buildBulkRequest(searchManager.getDefaultIndex(), listOfDocumentsToIndex);
            CompletableFuture<Void> currentIndexFuture = restClient.getAsyncClient().bulk(bulkRequest)
                    .thenAcceptAsync(bulkItemResponses -> {
                        try {
                            searchManager.handleIndexResponse(bulkItemResponses, listOfDocumentsToIndex);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).exceptionally(e -> {
                        LOGGER.error(
                                "An error occurred while indexing {} documents in current indexing list.",
                                toIndex.size(), e);
                        return null;
                    });

            // Request send, sort into queue
            // First, see if the previous future already finished
            CompletableFuture<Void> previousFuture = inflightFutures[index];
            if (previousFuture != null) {
                if (!previousFuture.isDone()) {
                    // Normally, the ES should be able to keep up. If it does not, just wait until there is some space in the ring buffer
                    LOGGER.info("Waiting for elasticsearch to process pending bulk requests...");
                    previousFuture.join();
                }
            }
            inflightFutures[index] = currentIndexFuture;
            index = (index + 1) % inflightFutures.length;
        }
    }

    private final State state = new State();
    private final Cleaner.Cleanable cleanable;
    private final int commitInterval;

    public BatchingIndexSubmittor() {
        this.commitInterval = 200;
        this.cleanable = CLEANER.register(this, state);
    }

    /**
     * @param estimatedTotalSize The estimated size of documents to index. Does not need to match the actual amount of submitted documents
     */
    public BatchingIndexSubmittor(int estimatedTotalSize) {
        if (estimatedTotalSize < 0) {
            throw new IllegalArgumentException("estimatedTotalSize must not be negative");
        }

        // Compute an ideal commit interval based on estimated size of elements to index
        // Try to strike a balance between
        // a) Not making enough bulk requests, thus having to wait a long time at the end for a large chunk => try to make at least 8 requests
        int elementsPerBatchRequest = estimatedTotalSize / 8;
        // b) Making too many requests, adding unnecessary overhead => set the minimum batch size to 20
        elementsPerBatchRequest = Math.max(20, elementsPerBatchRequest);
        // c) Growing the listOfDocumentsToIndex too large => set the maximum batch size to 200
        elementsPerBatchRequest = Math.min(200, elementsPerBatchRequest);
        this.commitInterval = elementsPerBatchRequest;
        this.cleanable = CLEANER.register(this, state);
    }

    @Override
    public void submitToIndex(String id, String jsonDocument, EsSearchManager searchManager) {
        if (state.closed) {
            throw new IllegalStateException("Attempting to use a closed " + this.getClass().getSimpleName());
        }

        state.searchManager = searchManager;
        Map<String, String> listOfDocumentsToIndex = state.listOfDocumentsToIndex;
        listOfDocumentsToIndex.put(id, jsonDocument);
        if (listOfDocumentsToIndex.size() >= commitInterval) {
            Map<String, String> toIndex = new HashMap<>(listOfDocumentsToIndex);
            listOfDocumentsToIndex.clear();
            state.sendDocumentsToIndex(toIndex);
        }
    }

    @Override
    public void close() {
        if (this.state.closed) {
            throw new IllegalStateException("Attempting to close a already closed " + this.getClass().getSimpleName());
        }
        this.state.closed = true;
        this.cleanable.clean();

        // Wait for all remaining documents to be received
        for (CompletableFuture<Void> inflightFuture : state.inflightFutures) {
            if (inflightFuture != null) {
                inflightFuture.join();
            }
        }
    }
}
