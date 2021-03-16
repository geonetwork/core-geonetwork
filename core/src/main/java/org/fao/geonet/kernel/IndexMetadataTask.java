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

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;
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

    private final String serviceName;
    private final ServiceManager serviceManager;
    private final List<?> _metadataIds;
    private final TransactionStatus _transactionStatus;
    private final Set<IndexMetadataTask> _batchIndex;
    private final SearchManager searchManager;
    private final AtomicInteger indexed;
    private final ConfigurableApplicationContext appContext;
    private User _user;

    /**
     * Setup index metadata task to be run.
     *
     * The context is used to look up beans for setup and configuration only. The task will create its own serviceContext
     * to be used during indexing.
     *
     * @param context           context object responsible for starting the activity
     * @param metadataIds       the metadata ids to index (either integers or strings)
     * @param batchIndex        Set used to track outstanding tasks
     * @param transactionStatus if non-null, wait for the transaction to complete before indexing
     * @param indexed           Used to track number of indexed records
     */
    public IndexMetadataTask(@Nonnull ServiceContext context, @Nonnull List<?> metadataIds, Set<IndexMetadataTask> batchIndex,
                      @Nullable TransactionStatus transactionStatus, @Nonnull AtomicInteger indexed) {
        this.indexed = indexed;
        this._transactionStatus = transactionStatus;
        this.serviceName = context.getService();
        this._metadataIds = metadataIds;
        this._batchIndex = batchIndex;
        this.serviceManager = context.getBean(ServiceManager.class);
        this.appContext = context.getApplicationContext();
        this.searchManager = context.getBean(SearchManager.class);

        batchIndex.add(this);

        if (context.getUserSession() != null) {
            this._user = context.getUserSession().getPrincipal();
        }
    }

    /**
     * Perform index task in a seperate thread.
     * <p>
     * Task waits for transactionStatus (if available) to be completed, and for servlet to be initialized.
     * </p>
     */
    @Override
    public void run() {
        ServiceContext indexMedataContext = serviceManager.createServiceContext(serviceName+":IndexTask", appContext);
        try {
            indexMedataContext.setUserSession(new UserSession());
            indexMedataContext.setAsThreadLocal();
            while (_transactionStatus != null && !_transactionStatus.isCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
            // poll context to see whether servlet is up yet
            while (!indexMedataContext.isServletInitialized()) {
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing..");
                }
                try {
                    Thread.sleep(10000); // sleep 10 seconds
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            DataManager dataManager = indexMedataContext.getBean(DataManager.class);
            // servlet up so safe to index all metadata that needs indexing
            for (Object metadataId : _metadataIds) {
                this.indexed.incrementAndGet();
                if (this.indexed.compareAndSet(500, 0)) {
                    try {
                        searchManager.forceIndexChanges();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    dataManager.indexMetadata(metadataId.toString(), false, null);
                } catch (Exception e) {
                    Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '" + metadataId + "': " + e.getMessage()
                        + "\n" + Util.getStackTrace(e));
                }
            }
            if (_user != null && indexMedataContext.getUserSession().getUserId() == null) {
                indexMedataContext.getUserSession().loginAs(_user);
            }
            searchManager.forceIndexChanges();
        } catch (IOException e) {
            Log.error(Geonet.INDEX_ENGINE, "Error occurred indexing metadata", e);
        } finally {
            _batchIndex.remove(this);
            indexMedataContext.clearAsThreadLocal();
            indexMedataContext.clear();
        }
    }
}
