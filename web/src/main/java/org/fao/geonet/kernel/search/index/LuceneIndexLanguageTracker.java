package org.fao.geonet.kernel.search.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import jeeves.utils.Log;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NRTCachingDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;


/**
 * Keeps track of the lucene indexes that currently exist so that we don't have to keep polling filesystem
 * 
 * @author jeichar
 */
public class LuceneIndexLanguageTracker {
    private final Map<String, NRTCachingDirectory> dirs = new HashMap<String, NRTCachingDirectory>();
    private final Map<String, TrackingIndexWriter> trackingWriters = new HashMap<String, TrackingIndexWriter>();
    private final Map<String, GeonetworkNRTManager> searchManagers = new HashMap<String, GeonetworkNRTManager>();
    private final Timer commitTimer;
    private final LuceneConfig luceneConfig;
    private final File indexContainingDir;
    private final TaxonomyIndexTracker taxonomyIndexTracker;
    private AtomicLong version = new AtomicLong(0);

    public LuceneIndexLanguageTracker(File indexContainingDir, File taxonomyDir, LuceneConfig luceneConfig) throws CorruptIndexException, LockObtainFailedException, IOException {
        this.taxonomyIndexTracker = new TaxonomyIndexTracker(taxonomyDir, luceneConfig);
        
        this.luceneConfig = luceneConfig;
        this.indexContainingDir = indexContainingDir;
        this.commitTimer = new Timer("Lucene index commit timer", true);
        commitTimer.scheduleAtFixedRate(new CommitTimerTask(), 60*1000, 60*1000);
        init(indexContainingDir, luceneConfig);
    }
    private void init(File indexContainingDir, LuceneConfig luceneConfig) throws IOException, CorruptIndexException,
            LockObtainFailedException {
        indexContainingDir.mkdirs();

        Set<File> indices = listIndices(indexContainingDir);
        for (File indexDir : indices) {
            open(indexDir);
        }
    }
    private void open(File indexDir) throws IOException, CorruptIndexException,
            LockObtainFailedException {
        indexDir.mkdirs();
        String language = indexDir.getName();
        
        Directory fsDir = FSDirectory.open(indexDir);
        double maxMergeSizeMD = luceneConfig.getMergeFactor();
        double maxCachedMB = luceneConfig.getRAMBufferSize();
        NRTCachingDirectory cachedFSDir = new NRTCachingDirectory(fsDir, maxMergeSizeMD, maxCachedMB);
        IndexWriterConfig conf = new IndexWriterConfig(Geonet.LUCENE_VERSION, SearchManager.getAnalyzer(language, false));
        ConcurrentMergeScheduler mergeScheduler = new ConcurrentMergeScheduler();
        conf.setMergeScheduler(mergeScheduler);
        IndexWriter writer = new IndexWriter(cachedFSDir, conf);
        TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(writer);
        GeonetworkNRTManager nrtManager = new GeonetworkNRTManager(luceneConfig, language, trackingIndexWriter, null, true, taxonomyIndexTracker);

        dirs.put(language, cachedFSDir);
        trackingWriters.put(language, trackingIndexWriter);
        searchManagers.put(language, nrtManager);
    }
    private Set<File> listIndices(File luceneDir) {
        Set<File> indices = new HashSet<File>();
        final File[] files = luceneDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (new File(file, "segments.gen").exists()) {
                    indices.add(file);
                }
            }
        }
        return indices;
    }
    private static String normalize( String locale ) {
        if(locale == null) {
            locale = "none";
        }
        return locale;
    }
    
    synchronized IndexAndTaxonomy aquire(String preferedLang, long versionToken) throws IOException {
        long finalVersion = versionToken;
        Map<Pair<Long, IndexSearcher>, GeonetworkNRTManager> searchers = new HashMap<Pair<Long, IndexSearcher>, GeonetworkNRTManager>((int) (searchManagers.size() * 1.5));
        IndexReader[] readers = new IndexReader[searchManagers.size()];
        int i = 1;
        boolean tokenExpired = false;
        for (GeonetworkNRTManager manager: searchManagers.values()) {
            if (!luceneConfig.useNRTManagerReopenThread() || Boolean.parseBoolean(System.getProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD))) {
                manager.maybeRefresh();
            }
            Pair<Long, IndexSearcher> indexSearcher = manager.acquire(versionToken);
            tokenExpired = tokenExpired || indexSearcher.one() != versionToken;

            if((preferedLang != null && preferedLang.equalsIgnoreCase(manager.language)) || i >= readers.length) {
                readers[0] = indexSearcher.two().getIndexReader();
            } else {
                readers[i] = indexSearcher.two().getIndexReader();
                i++;
            }
            searchers.put(indexSearcher, manager);
        }
        
        if(tokenExpired) {
            finalVersion = version.getAndIncrement();
            taxonomyIndexTracker.maybeRefresh();
            for (Map.Entry<Pair<Long, IndexSearcher>, GeonetworkNRTManager> entry: searchers.entrySet()) {
                entry.getValue().updateVersion(versionToken, finalVersion, entry.getKey().one());
            }
            
        }
        return new IndexAndTaxonomy(finalVersion, new GeonetworkMultiReader(readers, searchers), taxonomyIndexTracker.acquire());
    }

    synchronized void commit() throws CorruptIndexException, IOException {
        // before a writer commits the IndexWriter, it must commit the TaxonomyWriter.
        taxonomyIndexTracker.commit();
        for (TrackingIndexWriter writer : trackingWriters.values()) {
            writer.getIndexWriter().commit();
        }
    }
    synchronized void withWriter(Function function) throws CorruptIndexException, IOException {
        for (TrackingIndexWriter writer : trackingWriters.values()) {
            function.apply(taxonomyIndexTracker.writer(), writer);
        }
    }
    synchronized void addDocument(String language, Document doc, List<CategoryPath> categories) throws CorruptIndexException, LockObtainFailedException, IOException {
        open(language);
        // Add taxonomy first
        taxonomyIndexTracker.addDocument(doc, categories);
        trackingWriters.get(language).addDocument(doc);
    }
    synchronized void open(String language) throws CorruptIndexException, LockObtainFailedException, IOException {
        language = normalize(language);
        if(!trackingWriters.containsKey(language)) {
            File indexDir = new File(indexContainingDir, language);
            open(indexDir);
        }
    }
    
    public synchronized void reset() throws IOException {
        // reset taxonomy first
        taxonomyIndexTracker.reset();
        close(false);
        FileUtils.deleteDirectory(indexContainingDir);
        indexContainingDir.mkdirs();
        dirs.clear();
        trackingWriters.clear();
        searchManagers.clear();
        init(indexContainingDir, luceneConfig);
    }
    public synchronized void close(boolean closeTaxonomy) throws IOException {
        List<Throwable> errors = new ArrayList<Throwable>(5);

        if (closeTaxonomy) {
            // before a writer close's the IndexWriter, it must close() the TaxonomyWriter.
            taxonomyIndexTracker.close(errors);
        }
        
        for (GeonetworkNRTManager manager: searchManagers.values()) {
            try {
                manager.close();
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        for (TrackingIndexWriter writer: trackingWriters.values()) {
            try {
                writer.getIndexWriter().close(true);
            } catch (OutOfMemoryError e) {
                writer.getIndexWriter().close(true);
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        for (NRTCachingDirectory dir: dirs.values()) {
            try {
                dir.close();
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        
        if(!errors.isEmpty()) {
            for (Throwable throwable : errors) {
                Log.error(Geonet.LUCENE, "Failure while closing luceneIndexLanguageTracker", throwable);
            }
        }
    }
    public synchronized void optimize() throws CorruptIndexException, IOException {
        for (TrackingIndexWriter writer: trackingWriters.values()) {
            try {
                writer.getIndexWriter().forceMergeDeletes(true);
                writer.getIndexWriter().forceMerge(1,false);
            } catch (OutOfMemoryError e) {
                reset();
                throw new RuntimeException(e);
            }
        }
    }

    private class CommitTimerTask extends TimerTask {

        @Override
        public void run() {
            for (TrackingIndexWriter writer: trackingWriters.values()) {
                try {
                    try {
                        writer.getIndexWriter().commit();
                    } catch (Throwable e) {
                        Log.error(Geonet.LUCENE, "Error committing writer: "+writer, e);
                    }
                } catch (OutOfMemoryError e) {
                    try {
                        Log.error(Geonet.LUCENE, "OOM Error committing writer: "+writer, e);
                        reset();
                    } catch (IOException e1) {
                        Log.error(Geonet.LUCENE, "Error resetting lucene indices", e);
                    }
                    throw new RuntimeException(e);
                }
            }
        }
        
    }

}
