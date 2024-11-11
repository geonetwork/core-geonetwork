package org.fao.geonet.kernel.search.submission.batch;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public abstract class StateBase implements Runnable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);
    /**
     * Maximum number of inflight bulk requests before waiting for the Elasticsearch
     */
    private static final int MAX_INFLIGHT_INDEX_REQUESTS = 4;
    @SuppressWarnings("unchecked")
    protected final CompletableFuture<Void>[] inflightFutures = new CompletableFuture[MAX_INFLIGHT_INDEX_REQUESTS];
    protected int index = 0;
    protected boolean closed = false;
    protected EsSearchManager searchManager;

    @Override
    public final void run() {
        if (!closed) {
            LOGGER.error("BatchingIndexSubmittor was not closed before it was cleaned! Sending any remaining data");
        }
        cleanUp();
    }

    protected void queueFuture(CompletableFuture<Void> newFuture) {
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
        inflightFutures[index] = newFuture;
        index = (index + 1) % inflightFutures.length;
    }

    protected abstract void cleanUp();
}
