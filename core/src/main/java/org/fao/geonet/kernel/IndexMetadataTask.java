package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.search.SearchManager;
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
final class IndexMetadataTask implements Runnable {

    private final ServiceContext _context;
    private final List<?> _metadataIds;
    private final TransactionStatus _transactionStatus;
    private final Set<IndexMetadataTask> _batchIndex;
    private final SearchManager searchManager;
    private User _user;
    private final AtomicInteger indexed;

    /**
     * Constructor.
     *
     * @param context           context object
     * @param metadataIds       the metadata ids to index (either integers or strings)
     * @param batchIndex
     * @param transactionStatus if non-null, wait for the transaction to complete before indexing
     */
    IndexMetadataTask(@Nonnull ServiceContext context, @Nonnull List<?> metadataIds, Set<IndexMetadataTask> batchIndex,
                      @Nullable TransactionStatus transactionStatus, @Nonnull AtomicInteger indexed) {
        this.indexed = indexed;
        this._transactionStatus = transactionStatus;
        this._context = context;
        this._metadataIds = metadataIds;
        this._batchIndex = batchIndex;
        this.searchManager = context.getBean(SearchManager.class);

        batchIndex.add(this);

        if (context.getUserSession() != null) {
            this._user = context.getUserSession().getPrincipal();
        }
    }

    public void run() {
        try {
            _context.setAsThreadLocal();
            while (_transactionStatus != null && !_transactionStatus.isCompleted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return ;
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
                    try {
                        searchManager.forceIndexChanges();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    dataManager.indexMetadata(metadataId.toString(), false);
                } catch (Exception e) {
                    Log.error(Geonet.INDEX_ENGINE, "Error indexing metadata '" + metadataId + "': " + e.getMessage()
                                                   + "\n" + Util.getStackTrace(e));
                }
            }
            if (_user != null && _context.getUserSession().getUserId() == null) {
                _context.getUserSession().loginAs(_user);
            }
            searchManager.forceIndexChanges();
        } catch (IOException e) {
            Log.error(Geonet.INDEX_ENGINE, "Error occurred indexing metadata", e);
        } finally {
            _batchIndex.remove(this);
        }
    }
}
