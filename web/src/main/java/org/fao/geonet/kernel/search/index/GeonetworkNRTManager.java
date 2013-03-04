package org.fao.geonet.kernel.search.index;

import java.io.IOException;

import jeeves.utils.Log;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.apache.lucene.search.NRTManagerReopenThread;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherLifetimeManager;
import org.apache.lucene.search.SearcherLifetimeManager.PruneByAge;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneConfig;

import com.google.common.base.Predicate;

class GeonetworkNRTManager {

    private NRTManagerReopenThread reopenThread;
    private NRTManager actualManager;
    String language;
    private SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
    // taxonomyTracker is here so that we can commit it and refresh reader when 
    // we refresh
    private TaxonomyIndexTracker taxonomyTracker;

    public GeonetworkNRTManager(LuceneConfig luceneConfig, String language, TrackingIndexWriter writer, SearcherFactory searcherFactory,
            boolean applyAllDeletes, TaxonomyIndexTracker taxonomyTracker) throws IOException {
        this.taxonomyTracker = taxonomyTracker;
        actualManager = new NRTManager(writer, searcherFactory, applyAllDeletes);
        this.language = language;
        if (luceneConfig.useNRTManagerReopenThread()) {
            double targetMaxStaleSec = luceneConfig.getNRTManagerReopenThreadMaxStaleSec();
            double targetMinStaleSec = luceneConfig.getNRTManagerReopenThreadMinStaleSec();
            this.reopenThread = new NRTManagerReopenThread(actualManager, targetMaxStaleSec, targetMinStaleSec);
            reopenThread.setName("NRT Reopen Thread for " + language + " index");
            reopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
            reopenThread.setDaemon(true);
            reopenThread.start();
        }
    }

    public SearcherLifetimeManager getLifetimeManager() {
        return lifetimeManager;
    }

    @Override
    public String toString() {
        return "NRT Manager for " + language + " index";
    }

    protected void close() throws IOException {
        reopenThread.close();
        lifetimeManager.close();
        actualManager.close();
    }

    /**
     * Try to acquire a reader of a particular version. The version will be
     * mapped to the specific lifetime manager for this manager
     * 
     * @param versionToken
     * @param versionTracker 
     * @return either versionToken if there was an old version. If not then a
     *         new version will be returned which will have to be later
     *         registered with the single versionToken for the entire set of
     *         searchers.
     */
    public synchronized AcquireResult acquire(long versionToken, SearcherVersionTracker versionTracker) throws IOException {
        Long version = -1L;
        if(versionToken != -1) {
            version = versionTracker.get(this.language, versionToken);
            if (version == null) {
                version = -1L;
            }
        }
        IndexSearcher searcher = lifetimeManager.acquire(version);
        boolean lastVersionUpToDate = false;
        if (searcher == null) {
            searcher = actualManager.acquire();
            version = lifetimeManager.record(searcher);
            Long lastVersion = versionTracker.last(this.language);
            
            if (lastVersion != null) {
                IndexSearcher lastSearcher = lifetimeManager.acquire(lastVersion);
                if(lastSearcher == searcher) {
                    actualManager.release(searcher);
                    searcher = lastSearcher;
                    lastVersionUpToDate = true;
                }
            }
        } else {
            version = versionToken;
        }
        return new AcquireResult(version, lastVersionUpToDate, searcher, version != versionToken);
    }
    final class AcquireResult {
        final long version;
        final boolean lastVersionUpToDate;
        final IndexSearcher searcher;
        final boolean newSearcher;
        private AcquireResult(long version, boolean lastVersionUpToDate, IndexSearcher searcher, boolean newSearcher) {
            super();
            this.version = version;
            this.lastVersionUpToDate = lastVersionUpToDate;
            this.searcher = searcher;
            this.newSearcher = newSearcher;
        }
        
    }

    public boolean maybeRefresh() throws IOException {
        taxonomyTracker.maybeRefresh();
        return actualManager.maybeRefresh();
    }

    public void release(IndexSearcher searcher) throws IOException {
        actualManager.release(searcher);
    }

    public void purgeExpiredSearchers(SearcherVersionTracker versionTracker) {
        try {
            lifetimeManager.prune(new PruneByAge(1800.0));
        } catch (IOException e) {
            Log.error(Geonet.LUCENE, "error pruning SearcherLifetimeManager for: " + GeonetworkNRTManager.this, e);
        }
        // prune out the versionMapping now that lifetimeManager has
        // been pruned
        versionTracker.prune(this.language, new Predicate<Long>() {
            public boolean apply(Long version) {
                IndexSearcher searcher = lifetimeManager.acquire(version);
                if (searcher != null) {
                    try {
                        lifetimeManager.release(searcher);
                    } catch (IOException e) {
                        Log.error(Geonet.LUCENE, e.getMessage(), e);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public String getLanguage() {
        return language;
    }
}
