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

import java.lang.ref.Cleaner;
import java.util.concurrent.CompletableFuture;

public abstract class BatchingSubmitterBase<STATE extends StateBase> implements AutoCloseable {
    private static final int DESIRED_BATCH_COUNT = 25;
    private static final int MIN_BATCH_SIZE = 20;
    private static final int MAX_BATCH_SIZE = 100;
    private static final Cleaner CLEANER = Cleaner.create();
    protected final STATE state;
    protected final int commitInterval;
    private final Cleaner.Cleanable cleanable;

    protected BatchingSubmitterBase(final STATE state) {
        this.commitInterval = MAX_BATCH_SIZE;
        this.state = state;
        this.cleanable = CLEANER.register(this, state);
    }

    protected BatchingSubmitterBase(final STATE state, int estimatedTotalSize) {
        if (estimatedTotalSize < 0) {
            throw new IllegalArgumentException("estimatedTotalSize must not be negative");
        }

        // Compute an ideal commit interval based on estimated size of elements to index
        // Try to strike a balance between
        // a) Not making enough bulk requests, thus having to wait a long time at the end for a large chunk
        int elementsPerBatchRequest = estimatedTotalSize / DESIRED_BATCH_COUNT;
        // b) Making too many requests, adding unnecessary overhead
        elementsPerBatchRequest = Math.max(MIN_BATCH_SIZE, elementsPerBatchRequest);
        // c) Growing the listOfDocumentsToIndex too large, causing long time between user updates and large memory consumption
        elementsPerBatchRequest = Math.min(MAX_BATCH_SIZE, elementsPerBatchRequest);
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
