package org.fao.geonet.services.metadata.format.cache;

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Log;

import java.io.IOException;
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
        } catch (SQLException e) {
            StringBuilder exception = new StringBuilder(e.toString());
            exception.append('\n').append(strackTrace(e));
            for (Throwable next : e) {
                exception.append("\nNext Exception: '").append(next.getMessage()).append('\n').append(strackTrace(next));
            }
            Log.error(Geonet.FORMATTER, "Error writing to Formatter PersistenceStore: " + exception);
        } catch (IOException e) {
            Log.error(Geonet.FORMATTER, "Error writing to Formatter PersistenceStore", e);
        }
    }

    private String strackTrace(Throwable e) {
        final StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    @VisibleForTesting
    void doStore(Pair<Key, StoreInfoAndData> request) throws InterruptedException, IOException, SQLException {
        store.put(request.one(), request.two());
    }
}
