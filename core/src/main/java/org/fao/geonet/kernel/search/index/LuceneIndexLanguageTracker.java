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

package org.fao.geonet.kernel.search.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkNRTManager.AcquireResult;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Keeps track of the lucene indexes that currently exist so that we don't have to keep polling
 * filesystem
 *
 * @author jeichar
 */
public class LuceneIndexLanguageTracker {
    private final Map<String, Directory> dirs = new HashMap<String, Directory>();
    private final Map<String, TrackingIndexWriter> trackingWriters = new HashMap<String, TrackingIndexWriter>();
    private final Map<String, GeonetworkNRTManager> searchManagers = new HashMap<String, GeonetworkNRTManager>();
    private final SearcherVersionTracker versionTracker = new SearcherVersionTracker();
    private TaxonomyIndexTracker taxonomyIndexTracker;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private Lock lock = new ReentrantLock();
    private AtomicInteger _openReaderCounter = new AtomicInteger(0);
    private AtomicBoolean destroyed = new AtomicBoolean(false);

    public LuceneIndexLanguageTracker() {
        // used by spring
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

    private void lazyInit() {
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        LuceneConfig luceneConfig = context.getBean(LuceneConfig.class);
        DirectoryFactory directoryFactory = context.getBean(DirectoryFactory.class);
        ScheduledThreadPoolExecutor timer = context.getBean("timerThreadPool", ScheduledThreadPoolExecutor.class);

        if (!initialized.get()) {
            lock.lock();
            try {
                this.taxonomyIndexTracker = new TaxonomyIndexTracker(directoryFactory, luceneConfig);
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
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        DirectoryFactory directoryFactory = context.getBean(DirectoryFactory.class);

        try {
            Set<String> indices = directoryFactory.listIndices();
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
        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        LuceneConfig luceneConfig = context.getBean(LuceneConfig.class);
        DirectoryFactory directoryFactory = context.getBean(DirectoryFactory.class);

        Directory cachedFSDir = directoryFactory.createIndexDirectory(indexId, luceneConfig);
        IndexWriter writer = null;
        GeonetworkNRTManager nrtManager = null;
        TrackingIndexWriter trackingIndexWriter;
        boolean done = false;
        try {
            IndexWriterConfig conf = new IndexWriterConfig(Geonet.LUCENE_VERSION, SearchManager.getAnalyzer(indexId, false));
            ConcurrentMergeScheduler mergeScheduler = new ConcurrentMergeScheduler();
            conf.setMergeScheduler(mergeScheduler);
            writer = new IndexWriter(cachedFSDir, conf);
            trackingIndexWriter = new TrackingIndexWriter(writer);
            nrtManager = new GeonetworkNRTManager(luceneConfig, indexId,
                trackingIndexWriter, writer, null, true, taxonomyIndexTracker);
            done = true;
        } finally {
            if (!done) {
                IOUtils.closeQuietly(nrtManager);
                IOUtils.closeQuietly(writer);
                IOUtils.closeQuietly(cachedFSDir);
            }
        }
        dirs.put(indexId, cachedFSDir);
        trackingWriters.put(indexId, trackingIndexWriter);
        searchManagers.put(indexId, nrtManager);
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

            final ConfigurableApplicationContext context = ApplicationContextHolder.get();
            LuceneConfig luceneConfig = context.getBean(LuceneConfig.class);

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
        try {
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
        try {
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
        try {
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
                // Drop FacetField from the document in that case
                removeFacetFields(info.document);
                trackingWriters.get(language).addDocument(info.document);
            } else {
                trackingWriters.get(language).addDocument(docAfterFacetBuild);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove all facet fields from the provided document
     */
    Document removeFacetFields(Document doc) {
        List<IndexableField> listOfFields = doc.getFields();
        Iterator<IndexableField> iterator = listOfFields.iterator();
        while (iterator.hasNext()) {
            IndexableField field = iterator.next();
            if (field instanceof FacetField) {
                iterator.remove();
            }
        }
        return doc;
    }

    public void open(String language) throws IOException {
        lock.lock();
        try {
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
     * Wait for all readers to close, then delete all data from indices and clear out all caches.
     * Finally create empty indices.
     *
     * @param timeoutInMillis number of milliseconds to wait for reader to close before throwing
     *                        exception.
     */
    public void reset(long timeoutInMillis) throws Exception {
        lock.lock();
        try {
            lazyInit();
            final ConfigurableApplicationContext context = ApplicationContextHolder.get();
            DirectoryFactory directoryFactory = context.getBean(DirectoryFactory.class);

            waitForReadersToClose(timeoutInMillis);
            // reset taxonomy first
            taxonomyIndexTracker.reset();
            close(0, false);
            directoryFactory.resetIndex();
            init();
        } finally {
            lock.unlock();
        }
    }

    private void waitForReadersToClose(long timeoutInMillis) throws TimeoutException {
        final long startWait = System.currentTimeMillis();
        while (_openReaderCounter.get() > 0) {
            if (startWait + timeoutInMillis < System.currentTimeMillis()) {
                throw new TimeoutException("Waited for longer than " + timeoutInMillis + " and readers remain open");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // continue to obtain reader
            }
        }
    }

    @PreDestroy
    public synchronized void destroy() throws IOException {
        if (destroyed.get() == true) {
            return;
        }
        Log.warning(Geonet.LUCENE_TRACKING, "LuceneIndexLanguageTracker:destroy() called, closing indexes ...");
        try {
            close(TimeUnit.MINUTES.toMillis(1), true, false);
            Log.warning(Geonet.LUCENE_TRACKING, "LuceneIndexLanguageTracker:destroy() Done.");
            destroyed.set(true);
        } catch (Exception e) {
            Log.error(Geonet.LUCENE_TRACKING, "error occured while closing the indexes", e);
        }
    }

    /**
     * Close all indices and clear all caches.
     *
     * @param timeoutInMillis  the time to wait for all readers to close before closing indices
     * @param closeTaxonomy    if true close taxonomy reader.  Normally true unless called from
     *                         reset.
     * @param lazyInitRequired if true, call to lazyInit is made before cleaning up. Should be
     *                         avoided in case of destruction (because the beans needed in the
     *                         lazyInit call might be already destroyed).
     */
    public void close(long timeoutInMillis, boolean closeTaxonomy, boolean lazyInitRequired) throws IOException {
        lock.lock();
        try {
            if (lazyInitRequired)
                lazyInit();

            List<Throwable> errors = new ArrayList<Throwable>(5);
            try {
                waitForReadersToClose(timeoutInMillis);
            } catch (TimeoutException e) {
                Log.warning(Geonet.LUCENE_TRACKING, "not all Lucene readers closed after waiting " + timeoutInMillis + " ms.  Going ahead " +
                    "and closing indices");
            }
            if (closeTaxonomy) {
                // before a writer closes the IndexWriter, it must close() the
                // TaxonomyWriter.
                // taxonomyIndexTracker can be null if init() failed somehow (trying to get an unfreed lock)
                if (taxonomyIndexTracker != null) {
                    taxonomyIndexTracker.close(errors);
                }
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

    public void close(long timeoutInMillis, boolean closeTaxonomy) throws IOException {
        close(timeoutInMillis, closeTaxonomy, true);
    }

    public void optimize() throws Exception {
        lock.lock();
        try {
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

        // wait for the merges to be done outside of the lock to avoid locking writes to the indexes
        for (TrackingIndexWriter writer: trackingWriters.values()) {
            writer.getIndexWriter().waitForMerges();
        }

        // need to re-open the indexes for the files' size to actually reduce
        lock.lock();
        try{
            ArrayList<String> ids = new ArrayList<>(trackingWriters.keySet());
            for (String id : ids) {
                trackingWriters.get(id).getIndexWriter().close();
                searchManagers.get(id).close();
                dirs.get(id).close();
                openIndex(id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void deleteDocuments(final Term term) throws IOException {
        lock.lock();
        try {
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
