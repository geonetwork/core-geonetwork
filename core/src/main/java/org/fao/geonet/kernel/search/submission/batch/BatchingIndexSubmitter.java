/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
 
package org.fao.geonet.kernel.search.submission.batch;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.submission.IIndexSubmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An index submitter that batches documents into larger chunks and sends them asynchronously to the index.
 * When closing, this submitter sends the remaining item to the index and waits until all elasticsearch requests have been received,
 * so after closing there are no pending changes
 */
public class BatchingIndexSubmitter extends BatchingSubmitterBase<BatchingIndexSubmitter.State> implements IIndexSubmitter {
    protected static class State extends StateBase {
        private final Map<String, String> listOfDocumentsToIndex = new HashMap<>();

        @Override
        protected void cleanUp() {
            // Send any remaining pending documents
            if (!listOfDocumentsToIndex.isEmpty()) {
                sendDocumentsToIndex(listOfDocumentsToIndex);
            }
        }

        private void sendDocumentsToIndex(Map<String, String> toIndex) {
            EsRestClient restClient = searchManager.getClient();
            BulkRequest bulkRequest = restClient.buildIndexBulkRequest(searchManager.getDefaultIndex(), toIndex);
            CompletableFuture<Void> currentIndexFuture = restClient.getAsyncClient().bulk(bulkRequest)
                    .thenAcceptAsync(bulkItemResponses -> {
                        try {
                            searchManager.handleIndexResponse(bulkItemResponses, toIndex);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).exceptionally(e -> {
                        LOGGER.error(
                                "An error occurred while indexing {} documents in current indexing list.",
                                toIndex.size(), e);
                        return null;
                    });
            queueFuture(currentIndexFuture);
        }
    }

    public BatchingIndexSubmitter() {
        super(new State());
    }

    /**
     * @param estimatedTotalSize The estimated size of documents to index. Does not need to match the actual amount of submitted documents
     */
    public BatchingIndexSubmitter(int estimatedTotalSize) {
        super(new State(), estimatedTotalSize);
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
}
