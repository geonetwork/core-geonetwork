package org.fao.geonet.kernel.search.index;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final LuceneConfig luceneConfig;
    private final DirectoryFactory _directoryFactory;
    private Timer commitTimer = null;
    private TaxonomyIndexTracker taxonomyIndexTracker;
    private final SearcherVersionTracker versionTracker = new SearcherVersionTracker();
    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Autowired
    public LuceneIndexLanguageTracker(DirectoryFactory directoryFactory, LuceneConfig luceneConfig)
            throws Exception {
        this.luceneConfig = luceneConfig;
        this._directoryFactory = directoryFactory;
    }

    private void lazyInit() {
        if (!initialized.get()) {
            synchronized (this) {
                try {
                    this.taxonomyIndexTracker = new TaxonomyIndexTracker(_directoryFactory, luceneConfig);
                    init();
                    this.commitTimer = new Timer("Lucene index commit timer", true);
                    commitTimer.scheduleAtFixedRate(new CommitTimerTask(), TimeUnit.SECONDS.toMillis(30), TimeUnit.SECONDS.toMillis(30));
                    commitTimer.scheduleAtFixedRate(new PurgeExpiredSearchersTask(), TimeUnit.SECONDS.toMillis(30),
                            TimeUnit.SECONDS.toMillis(30));

                    initialized.set(true);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
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
            Log.error(Geonet.INDEX_ENGINE, "An error occurred while openning lucene index readers/writers", e);
            close(true);
            throw e;
        }
    }

    private void openIndex(String indexId) throws IOException, CorruptIndexException, LockObtainFailedException {
        String language = indexId;

        Directory fsDir = null;
        Directory cachedFSDir = null;
        IndexWriter writer = null;
        GeonetworkNRTManager nrtManager = null;
        TrackingIndexWriter trackingIndexWriter;
        try {
            cachedFSDir = _directoryFactory.createIndexDirectory(language, luceneConfig);
            IndexWriterConfig conf = new IndexWriterConfig(Geonet.LUCENE_VERSION, SearchManager.getAnalyzer(language, false));
            ConcurrentMergeScheduler mergeScheduler = new ConcurrentMergeScheduler();
            conf.setMergeScheduler(mergeScheduler);
            writer = new IndexWriter(cachedFSDir, conf);
            trackingIndexWriter = new TrackingIndexWriter(writer);
            nrtManager = new GeonetworkNRTManager(luceneConfig, language,
                    trackingIndexWriter, null, true, taxonomyIndexTracker);
        } catch (CorruptIndexException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            IOUtils.closeQuietly(fsDir);
            throw e;
        } catch (LockObtainFailedException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            IOUtils.closeQuietly(fsDir);
            throw e;
        } catch (IOException e) {
            IOUtils.closeQuietly(nrtManager);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(cachedFSDir);
            IOUtils.closeQuietly(fsDir);
            throw e;
        }
        dirs.put(language, cachedFSDir);
        trackingWriters.put(language, trackingIndexWriter);
        searchManagers.put(language, nrtManager);
    }

    private static String normalize(String locale) {
        if (locale == null) {
            locale = "none";
        }
        return locale;
    }

    /**
     * Get {@linkplain org.apache.lucene.index.MultiReader}.
     *
     * @param versionToken A token indicating which state of search should be obtained
     * @return an index reader for reading from all indices
     */
    public synchronized IndexAndTaxonomy acquire(final String preferedLang, final long versionToken) throws IOException {
        lazyInit();

        if (!luceneConfig.useNRTManagerReopenThread()
            || Boolean.parseBoolean(System.getProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD))) {
            maybeRefreshBlocking();
        }


        long finalVersion = versionToken;
        Map<AcquireResult, GeonetworkNRTManager> searchers = new HashMap<AcquireResult, GeonetworkNRTManager>(
                (int) (searchManagers.size() * 1.5));
        IndexReader[] readers = new IndexReader[searchManagers.size()];
        int i = 1;
        boolean tokenExpired = false;
        boolean lastVersionUpToDate = true;
        for (GeonetworkNRTManager manager : searchManagers.values()) {
            AcquireResult result = manager.acquire(versionToken, versionTracker);
            lastVersionUpToDate = lastVersionUpToDate && result.lastVersionUpToDate;
            tokenExpired = tokenExpired || result.newSearcher;

            if ((preferedLang != null && preferedLang.equalsIgnoreCase(manager.language)) || i >= readers.length) {
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
        return new IndexAndTaxonomy(finalVersion, new GeonetworkMultiReader(readers, searchers),
                taxonomyIndexTracker.acquire());
    }

    /**
     * Block until a fresh index reader can be acquired.
     */
    public void maybeRefreshBlocking() throws IOException {
        commit();
        for (GeonetworkNRTManager manager : searchManagers.values()) {
            manager.maybeRefreshBlocking();
        }
    }

    synchronized void commit() throws CorruptIndexException, IOException {
        lazyInit();
        // before a writer commits the IndexWriter, it must commit the
        // TaxonomyWriter.
        taxonomyIndexTracker.commit();
        for (TrackingIndexWriter writer : trackingWriters.values()) {
            writer.getIndexWriter().commit();
        }
    }

    synchronized void withWriter(Function function) throws CorruptIndexException, IOException {
        lazyInit();
        for (TrackingIndexWriter writer : trackingWriters.values()) {
            function.apply(taxonomyIndexTracker.writer(), writer);
        }
    }

    public synchronized void addDocument(String language, Document doc, Collection<CategoryPath> categories)
            throws CorruptIndexException, LockObtainFailedException, IOException {
        lazyInit();
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Adding document to " + language + " index");
        }
        open(language);
        // Add taxonomy first
        if (categories.size() > 0) {
            taxonomyIndexTracker.addDocument(doc, categories);
        }
        trackingWriters.get(language).addDocument(doc);
    }

    public synchronized void open(String language) throws CorruptIndexException, LockObtainFailedException, IOException {
        lazyInit();
        language = normalize(language);
        if (!trackingWriters.containsKey(language)) {
            openIndex(language);
        }
    }

    public synchronized void reset() throws Exception {
        lazyInit();
        // reset taxonomy first
        taxonomyIndexTracker.reset();
        close(false);
        _directoryFactory.resetIndex();
        init();
    }

    public synchronized void close(boolean closeTaxonomy) throws IOException {
        lazyInit();
        List<Throwable> errors = new ArrayList<Throwable>(5);


        if (closeTaxonomy) {
            // before a writer close's the IndexWriter, it must close() the
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
    }

    public synchronized void optimize() throws Exception {
        lazyInit();
        for (TrackingIndexWriter writer : trackingWriters.values()) {
            try {
                writer.getIndexWriter().forceMergeDeletes(true);
                writer.getIndexWriter().forceMerge(1, false);
            } catch (OutOfMemoryError e) {
                reset();
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteDocuments(final Term term) throws IOException {
        lazyInit();
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "deleting term '" + term + "' from index");
        }
        withWriter(new Function() {
            @Override
            public void apply(TaxonomyWriter taxonomyWriter, TrackingIndexWriter input) throws CorruptIndexException, IOException {
                input.deleteDocuments(term);
            }
        });
    }

    private class CommitTimerTask extends TimerTask {

        @Override
        public void run() {
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
                        reset();
                    } catch (Exception e1) {
                        Log.error(Geonet.LUCENE, "Error resetting lucene indices", e);
                    }
                    throw new RuntimeException(e);
                }
            }
        }

    }

    private class PurgeExpiredSearchersTask extends TimerTask {
        @Override
        public void run() {
            synchronized (LuceneIndexLanguageTracker.this) {
                Collection<GeonetworkNRTManager> values = searchManagers.values();
                for (GeonetworkNRTManager geonetworkNRTManager : values) {
                    geonetworkNRTManager.purgeExpiredSearchers(versionTracker);
                }
            }
            Log.info(Geonet.LUCENE, "Done running PurgeExpiredSearchersTask. " + versionTracker.size()
                                    + " versions still cached.");

        }
    }
}
