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

package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.submission.BatchingIndexSubmittor;
import org.fao.geonet.utils.Log;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A runnable for indexing multiple metadata in a separate thread.
 */
public final class IndexMetadataTask implements Runnable {

    private final ServiceContext _context;
    private final List<?> _metadataIds;
    private final TransactionStatus _transactionStatus;
    private final Set<IndexMetadataTask> _batchIndex;
    private final EsSearchManager searchManager;
    private final AtomicInteger indexed;
    private User _user;

    /**
     * Constructor.
     *
     * @param context           context object
     * @param metadataIds       the metadata ids to index (either integers or strings)
     * @param transactionStatus if non-null, wait for the transaction to complete before indexing
     */
    public IndexMetadataTask(@Nonnull ServiceContext context, @Nonnull List<?> metadataIds, Set<IndexMetadataTask> batchIndex,
                      @Nullable TransactionStatus transactionStatus, @Nonnull AtomicInteger indexed) {
        this.indexed = indexed;
        this._transactionStatus = transactionStatus;
        this._context = context;
        this._metadataIds = metadataIds;
        this._batchIndex = batchIndex;
        this.searchManager = context.getBean(EsSearchManager.class);

        batchIndex.add(this);

        if (context.getUserSession() != null) {
            this._user = context.getUserSession().getPrincipal();
        }
    }

    public void run() {
        try (BatchingIndexSubmittor batchingIndexSubmittor = new BatchingIndexSubmittor()) {
            _context.setAsThreadLocal();
            while (_transactionStatus != null && !_transactionStatus.isCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
            // poll context to see whether servlet is up yet
            while (!_context.isServletInitialized()) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing..");
                }
                try {
                    Thread.sleep(10000); // sleep 10 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            DataManager dataManager = _context.getBean(DataManager.class);
            // servlet up so safe to index all metadata that needs indexing
            for (Object metadataId : _metadataIds) {
                this.indexed.incrementAndGet();
                if (this.indexed.compareAndSet(500, 0)) {
                    searchManager.forceIndexChanges();
                }

                try {
                    dataManager.indexMetadata(metadataId.toString(), batchingIndexSubmittor);
                } catch (Exception e) {
                    Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '" + metadataId + "': " + e.getMessage()
                        + "\n" + Util.getStackTrace(e));
                }
            }
            if (_user != null && _context.getUserSession().getUserId() == null) {
                _context.getUserSession().loginAs(_user);
            }
        } finally {
            _batchIndex.remove(this);
        }
    }
}
