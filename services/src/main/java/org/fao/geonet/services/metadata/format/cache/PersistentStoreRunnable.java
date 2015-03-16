package org.fao.geonet.services.metadata.format.cache;

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * Runnable responsible for monitoring the FormatterCache storeRequest queue and pushing the requests to the persistent store.
 *
 * @author Jesse on 3/5/2015.
 */
public class PersistentStoreRunnable implements Runnable {
    private final BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests;
    private final PersistentStore store;

    public PersistentStoreRunnable(BlockingQueue<Pair<Key, StoreInfoAndDataLoadResult>> storeRequests, PersistentStore store) {
        this.storeRequests = storeRequests;
        this.store = store;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                final Pair<Key, StoreInfoAndDataLoadResult> request = storeRequests.take();
                processStoreRequest(request);
            }
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    void processStoreRequest(Pair<Key, StoreInfoAndDataLoadResult> request) {
        try {
            doStore(request);
        } catch (SQLException e) {
            StringBuilder exception = new StringBuilder(e.toString());
            exception.append('\n').append(strackTrace(e));
            for (Throwable next : e) {
                exception.append("\nNext Exception: '").append(next.getMessage()).append('\n').append(strackTrace(next));
            }
            Log.error(Geonet.FORMATTER, "Error writing to Formatter PersistenceStore: " + exception);
        } catch (Exception e) {
            Log.error(Geonet.FORMATTER, "Error writing to Formatter PersistenceStore", e);
        }
    }

    private String strackTrace(Throwable e) {
        final StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    @VisibleForTesting
    void doStore(Pair<Key, StoreInfoAndDataLoadResult> request) throws Exception {
        Key key = request.one();
        StoreInfoAndDataLoadResult result = request.two();
        store.put(key, result);

        while (result.getKey() != null && result.getToCache() != null) {
            key = result.getKey();
            result = result.getToCache().call();
            store.put(key, result);
        }
    }
}
