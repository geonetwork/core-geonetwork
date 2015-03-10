package org.fao.geonet.kernel.search.index;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Keeps track of the lucene indexes that currently exist so that we don't have
 * to keep polling filesystem
 * 
 * @author jeichar
 */
public class LuceneIndexLanguageTracker {
	private final Map<String, Directory> dirs = new HashMap<String, Directory>();
	private final Map<String, TrackingIndexWriter> trackingWriters = new HashMap<String, TrackingIndexWriter>();
	private final Map<String, GeonetworkNRTManager> searchManagers = new HashMap<String, GeonetworkNRTManager>();
    @Autowired
	private LuceneConfig luceneConfig;
    @Autowired
    private DirectoryFactory _directoryFactory;
    @Qualifier("timerThreadPool")
    @Autowired
    private ScheduledThreadPoolExecutor timer;

	private TaxonomyIndexTracker taxonomyIndexTracker;
	private final SearcherVersionTracker versionTracker = new SearcherVersionTracker();
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private Lock lock = new ReentrantLock();
	private AtomicInteger _openReaderCounter = new AtomicInteger(0);

    public LuceneIndexLanguageTracker() {
        // used by spring
    }
    public LuceneIndexLanguageTracker(FSDirectoryFactory directoryFactory, LuceneConfig luceneConfig) {
        this._directoryFactory = directoryFactory;
        this.luceneConfig = luceneConfig;
    }

    private void lazyInit() {
        if (!initialized.get()) {
            lock.lock();
            try {
                this.taxonomyIndexTracker = new TaxonomyIndexTracker(_directoryFactory, luceneConfig);
                init();

                if (timer != null) {
                    timer.scheduleAtFixedRate(new CommitTimerTask(), 30, 30, TimeUnit.SECONDS);
                    timer.scheduleAtFixedRate(new PurgeExpiredSearchersTask(), 30, 30, TimeUnit.SECONDS);
                }
                initialized.set(true);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    private void init() throws Exception {
        try {
            Set<String> indices = _directoryFactory.listIndices();
            for (String indexDir : indices) {
                openIndex(indexDir);
            }
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE, "An error occurred while opening lucene index readers/writers", e);
            close(60000, true);
            throw e;
        }
    }

    private void openIndex(String indexId) throws IOException {

        Directory cachedFSDir = null;
        IndexWriter writer = null;
        GeonetworkNRTManager nrtManager = null;
        TrackingIndexWriter trackingIndexWriter;
        try {
            cachedFSDir = _directoryFactory.createIndexDirectory(indexId, luceneConfig);
            IndexWriterConfig conf = new IndexWriterConfig(Geonet.LUCENE_VERSION, SearchManager.getAnalyzer(indexId, false));
            ConcurrentMergeScheduler mergeScheduler = new ConcurrentMergeScheduler();
            conf.setMergeScheduler(mergeScheduler);
            writer = new IndexWriter(cachedFSDir, conf);
            trackingIndexWriter = new TrackingIndexWriter(writer);
            nrtManager = new GeonetworkNRTManager(luceneConfig, indexId,
                    trackingIndexWriter, writer, null, true, taxonomyIndexTracker);
        } catch (CorruptIndexException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            throw e;
        } catch (LockObtainFailedException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            throw e;
        } catch (IOException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            throw e;
        }
        dirs.put(indexId, cachedFSDir);
        trackingWriters.put(indexId, trackingIndexWriter);
        searchManagers.put(indexId, nrtManager);
    }

    private static String normalize(String locale) {
        if (locale == null) {
            locale = "none";
        }
        locale = locale.toLowerCase();
        switch (locale) {
            case "deu":
                locale = "ger";
                break;
            case "fra":
                locale = "fre";
                break;
            default:
                // do nothing
        }
        return locale;
    }

    /**
     * Get {@linkplain org.apache.lucene.index.MultiReader}.
     *
     * @param versionToken A token indicating which state of search should be obtained
     * @return an index reader for reading from all indices
     */
    public IndexAndTaxonomy acquire(final String preferredLang, final long versionToken) throws IOException {
        lock.lock();
        try {
            lazyInit();


            if (!luceneConfig.useNRTManagerReopenThread()
                || Boolean.parseBoolean(System.getProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD))) {
                maybeRefreshBlocking();
            }


            long finalVersion = versionToken;
            Map<AcquireResult, GeonetworkNRTManager> searchers = new HashMap<>(
                    (int) (searchManagers.size() * 1.5));
            IndexReader[] readers = new IndexReader[searchManagers.size()];
            int i = 1;
            boolean tokenExpired = false;
            boolean lastVersionUpToDate = true;
            for (GeonetworkNRTManager manager : searchManagers.values()) {
                AcquireResult result = manager.acquire(versionToken, versionTracker);
                lastVersionUpToDate = lastVersionUpToDate && result.lastVersionUpToDate;
                tokenExpired = tokenExpired || result.newSearcher;

                if ((preferredLang != null && preferredLang.equalsIgnoreCase(manager.language)) || i >= readers.length) {
                    readers[0] = result.searcher.getIndexReader();
                } else {
                    readers[i] = result.searcher.getIndexReader();
                    i++;
                }
                searchers.put(result, manager);
            }

            if (tokenExpired) {
                if (lastVersionUpToDate) {
                    finalVersion = versionTracker.lastVersion();
                } else {
                    taxonomyIndexTracker.maybeRefresh();
                    finalVersion = versionTracker.register(searchers);
                }

            }
            return new IndexAndTaxonomy(finalVersion, new GeonetworkMultiReader(_openReaderCounter, readers, searchers),
                    taxonomyIndexTracker.acquire());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Block until a fresh index reader can be acquired.
     */
    public void maybeRefreshBlocking() throws IOException {
        lock.lock();
        try {
            lazyInit();
            commit();
            for (GeonetworkNRTManager manager : searchManagers.values()) {
                manager.maybeRefreshBlocking();
            }
        } finally {
            lock.unlock();
        }
    }

    public void commit() throws IOException {
        lock.lock();
        try{
            lazyInit();
            // before a writer commits the IndexWriter, it must commit the
            // TaxonomyWriter.
            taxonomyIndexTracker.commit();
            for (TrackingIndexWriter writer : trackingWriters.values()) {
                writer.getIndexWriter().commit();
            }
        } finally {
            lock.unlock();
        }
    }

    void withWriter(Function function) throws IOException {
        lock.lock();
        try{
            lazyInit();
            for (TrackingIndexWriter writer : trackingWriters.values()) {
                function.apply(taxonomyIndexTracker.writer(), writer);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addDocument(IndexInformation info)
            throws IOException {
        lock.lock();
        try{
            lazyInit();
            final String language = normalize(info.language);
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Adding document to " + language + " index");
            }
            open(language);
            // Add taxonomy first
            Document docAfterFacetBuild = info.document;
            docAfterFacetBuild = taxonomyIndexTracker.addDocument(info.document, info.taxonomy);
            // Index the document returned after the facets are built by the taxonomy writer
            if (docAfterFacetBuild == null) {
                trackingWriters.get(language).addDocument(info.document);
            } else {
                trackingWriters.get(language).addDocument(docAfterFacetBuild);
            }
        } finally {
            lock.unlock();
        }
    }

    public void open(String language) throws IOException {
        lock.lock();
        try{
            lazyInit();
            language = normalize(language);
            if (!trackingWriters.containsKey(language)) {
                openIndex(language);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wait for all readers to close, then delete all data from indices and clear out all caches.  Finally create empty indices.
     *
     * @param timeoutInMillis number of milliseconds to wait for reader to close before throwing exception.
     * @throws Exception
     */
    public void reset(long timeoutInMillis) throws Exception {
        lock.lock();
        try{
            lazyInit();

            waitForReadersToClose(timeoutInMillis);
            // reset taxonomy first
            taxonomyIndexTracker.reset();
            close(0, false);
            _directoryFactory.resetIndex();
            init();
        } finally {
            lock.unlock();
        }
    }

    private void waitForReadersToClose(long timeoutInMillis) throws TimeoutException {
        final long startWait = System.currentTimeMillis();
        while(_openReaderCounter.get() > 0) {
            if (startWait + timeoutInMillis < System.currentTimeMillis()) {
                throw new TimeoutException("Waited for longer than "+timeoutInMillis+" and readers remain open");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // continue to obtain reader
            }
        }
    }

    /**
     * Close all indices and clear all caches.
     *
     * @param timeoutInMillis the time to wait for all readers to close before closing indices
     * @param closeTaxonomy if true close taxonomy reader.  Normally true unless called from reset.
     * @throws IOException
     */
    public void close(long timeoutInMillis, boolean closeTaxonomy) throws IOException {
        lock.lock();
        try{
            lazyInit();

            List<Throwable> errors = new ArrayList<Throwable>(5);
            try {
                waitForReadersToClose(timeoutInMillis);
            } catch (TimeoutException e) {
                Log.warning(Geonet.LUCENE_TRACKING, "not all Lucene readers closed after waiting "+timeoutInMillis+" ms.  Going ahead " +
                                                    "and closing indices");
            }
            if (closeTaxonomy) {
                // before a writer closes the IndexWriter, it must close() the
                // TaxonomyWriter.
                taxonomyIndexTracker.close(errors);
            }

            for (GeonetworkNRTManager manager : searchManagers.values()) {
                try {
                    manager.close();
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
            for (TrackingIndexWriter writer : trackingWriters.values()) {
                try {
                    writer.getIndexWriter().close(true);
                } catch (OutOfMemoryError e) {
                    writer.getIndexWriter().close(true);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
            for (Directory dir : dirs.values()) {
                try {
                    dir.close();
                } catch (Throwable e) {
                    errors.add(e);
                }
            }

            dirs.clear();
            trackingWriters.clear();
            searchManagers.clear();

            if (!errors.isEmpty()) {
                for (Throwable throwable : errors) {
                    Log.error(Geonet.LUCENE, "Failure while closing luceneIndexLanguageTracker", throwable);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void optimize() throws Exception {
        lock.lock();
        try{
            lazyInit();
            for (TrackingIndexWriter writer : trackingWriters.values()) {
                try {
                    writer.getIndexWriter().forceMergeDeletes(true);
                    writer.getIndexWriter().forceMerge(1, false);
                } catch (OutOfMemoryError e) {
                    reset(TimeUnit.MINUTES.toMillis(1));
                    throw new RuntimeException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void deleteDocuments(final Term term) throws IOException {
        lock.lock();
        try{
            lazyInit();
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "deleting term '" + term + "' from index");
            }
            withWriter(new Function() {
                @Override
                public void apply(TaxonomyWriter taxonomyWriter, TrackingIndexWriter input) throws IOException {
                    input.deleteDocuments(term);
                }
            });
        } finally {
            lock.unlock();
        }
    }


    private class CommitTimerTask implements Runnable {

        @Override
        public void run() {
            lock.lock();
            try {
                Log.debug(Geonet.LUCENE, "Running Lucene committer timer");
                for (TrackingIndexWriter writer : trackingWriters.values()) {
                    try {
                        try {
                            writer.getIndexWriter().commit();
                        } catch (Throwable e) {
                            Log.error(Geonet.LUCENE, "Error committing writer: " + writer, e);
                        }
                    } catch (OutOfMemoryError e) {
                        try {
                            Log.error(Geonet.LUCENE, "OOM Error committing writer: " + writer, e);
                            reset(TimeUnit.MINUTES.toMillis(1));
                        } catch (Exception e1) {
                            Log.error(Geonet.LUCENE, "Error resetting lucene indices", e);
                        }
                        throw new RuntimeException(e);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

    }

    private class PurgeExpiredSearchersTask implements Runnable {
        @Override
        public void run() {
            lock.lock();
            try {
                Collection<GeonetworkNRTManager> values = searchManagers.values();
                for (GeonetworkNRTManager geonetworkNRTManager : values) {
                    geonetworkNRTManager.purgeExpiredSearchers(versionTracker);
                }
            } finally {
                lock.unlock();
            }
            Log.info(Geonet.LUCENE, "Done running PurgeExpiredSearchersTask. " + versionTracker.size()
                                    + " versions still cached.");

        }
    }
}
