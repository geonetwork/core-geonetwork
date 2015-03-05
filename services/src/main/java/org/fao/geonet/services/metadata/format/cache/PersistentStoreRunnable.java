package org.fao.geonet.services.metadata.format.cache;

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.domain.Pair;

import java.util.concurrent.BlockingQueue;

/**
 * Runnable responsible for monitoring the FormatterCache storeRequest queue and pushing the requests to the persistent store.
 *
 * @author Jesse on 3/5/2015.
 */
public class PersistentStoreRunnable implements Runnable {
    private final BlockingQueue<Pair<Key, StoreInfoAndData>> storeRequests;
    private final PersistentStore store;

    public PersistentStoreRunnable(BlockingQueue<Pair<Key, StoreInfoAndData>> storeRequests, PersistentStore store) {
        this.storeRequests = storeRequests;
        this.store = store;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                final Pair<Key, StoreInfoAndData> request = storeRequests.take();
                doStore(request);
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    @VisibleForTesting
    void doStore(Pair<Key, StoreInfoAndData> request) throws InterruptedException {
        store.put(request.one(), request.two());
    }
}
