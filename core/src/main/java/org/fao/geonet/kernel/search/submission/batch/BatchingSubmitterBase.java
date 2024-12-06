package org.fao.geonet.kernel.search.submission.batch;

import java.lang.ref.Cleaner;
import java.util.concurrent.CompletableFuture;

public abstract class BatchingSubmitterBase<STATE extends StateBase> implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    protected final STATE state;
    protected final int commitInterval;
    private final Cleaner.Cleanable cleanable;

    protected BatchingSubmitterBase(final STATE state) {
        this.commitInterval = 200;
        this.state = state;
        this.cleanable = CLEANER.register(this, state);
    }

    protected BatchingSubmitterBase(final STATE state, int estimatedTotalSize) {
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
        this.state = state;
        this.cleanable = CLEANER.register(this, state);
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
