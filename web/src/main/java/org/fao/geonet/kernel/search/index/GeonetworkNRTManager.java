package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.fao.geonet.kernel.search.spatial.Pair;

class GeonetworkNRTManager {

    private NRTManagerReopenThread reopenThread;
    private NRTManager actualManager;
    String language;
    private SearcherLifetimeManager lifetimeManager = new SearcherLifetimeManager();
    private Map<Long, Long> versionMapping = new HashMap<Long, Long>();
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
            this.reopenThread = new GeonetworkNRTManagerReopenThread(actualManager, targetMaxStaleSec, targetMinStaleSec);
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
     * @return either versionToken if there was an old version. If not then a
     *         new version will be returned which will have to be later
     *         registered with the single versionToken for the entire set of
     *         searchers.
     */
    public synchronized Pair<Long, IndexSearcher> acquire(long versionToken) throws IOException {
        Long version = versionMapping.get(versionToken);
        if (version == null) {
            version = -1L;
        }
        IndexSearcher searcher = lifetimeManager.acquire(version);
        if (searcher == null) {
            searcher = actualManager.acquire();
            version = lifetimeManager.record(searcher);
        } else {
            version = versionToken;
        }
        return Pair.read(version, searcher);
    }

    /**
     * Map a single version token to a specific version token that is specific
     * to this class.
     * 
     * @param oldVersion
     *            the old version to replace with the new version. Might not be
     *            in mapping
     * @param newVersion
     *            the new version to assocate with the searcherSpecificVersion
     * @param searcherSpecificVersion
     */
    public synchronized void updateVersion(long oldVersion, long newVersion, Long searcherSpecificVersion) {
        versionMapping.remove(oldVersion);
        versionMapping.put(newVersion, searcherSpecificVersion);
    }

    public boolean maybeRefresh() throws IOException {
        taxonomyTracker.maybeRefresh();
        return actualManager.maybeRefresh();
    }

    public void release(IndexSearcher searcher) throws IOException {
        actualManager.release(searcher);
    }

    
    /**
     * ALso prunes the SearcherLifetimeManager
     */
    private final class GeonetworkNRTManagerReopenThread extends NRTManagerReopenThread {
        long lastPrune = System.currentTimeMillis();
        private GeonetworkNRTManagerReopenThread(NRTManager manager, double targetMaxStaleSec, double targetMinStaleSec) {
            super(manager, targetMaxStaleSec, targetMinStaleSec);
        }

        @Override
        public void run() {
            super.run();
            // only prune every 30 seconds.
            if (System.currentTimeMillis() - lastPrune < (30000)) {
                try {
                    lifetimeManager.prune(new PruneByAge(3600.0));
                } catch (IOException e) {
                    Log.error(Geonet.LUCENE, "error pruning SearcherLifetimeManager for: " + GeonetworkNRTManager.this, e);
                }
                // prune out the versionMapping now that lifetimeManager has
                // been pruned
                Iterator<Long> iter = versionMapping.values().iterator();
                while (iter.hasNext()) {
                    Long version = iter.next();
                    IndexSearcher searcher = lifetimeManager.acquire(version);
                    if (searcher != null) {
                        try {
                            lifetimeManager.release(searcher);
                        } catch (IOException e) {
                            Log.error(Geonet.LUCENE, e.getMessage(), e);
                        }
                    } else {
                        iter.remove();
                    }
                }
            }
        }
    }
}
