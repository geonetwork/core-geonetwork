/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.formatters.cache;

import com.google.common.annotations.VisibleForTesting;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * Runnable responsible for monitoring the FormatterCache storeRequest queue and pushing the
 * requests to the persistent store.
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
