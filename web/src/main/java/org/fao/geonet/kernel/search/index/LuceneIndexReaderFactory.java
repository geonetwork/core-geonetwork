package org.fao.geonet.kernel.search.index;

import java.io.IOException;

import org.apache.lucene.index.MultiReader;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;

/**
 * Utility class to get/refresh readers for SearchManager class Works by opening an IndexReader at
 * startup and keeping that reader open. It is never closed. Users of this class call getReader
 * which increments the ref count on the IndexReader when they need an IndexReader and when
 * finished, releaseReader which decrements the ref count. Any call to getReader may reopen the
 * index if changes have been made. Idea and some code taken from SearchManager.java, Lucene In
 * Action 2, Manning Books
 */

public class LuceneIndexReaderFactory {

    private LuceneIndexLanguageTracker tracker;

    // ===========================================================================
    // Constructor
    public LuceneIndexReaderFactory( LuceneIndexLanguageTracker tracker ) {
        this.tracker = tracker;
    }

    // ===========================================================================
    // Public interface methods

    /**
     * Get {@linkplain MultiReader}. If
     *
     * @param versionToken A token indicating which state of search should be obtained
     * @return an index reader for reading from all indices
     */
    public IndexAndTaxonomy aquire(String preferedLang, long versionToken) throws IOException {
        return tracker.aquire(preferedLang, versionToken);
    }
    
    public void release (GeonetworkMultiReader reader) throws InterruptedException, IOException {
        reader.releaseToNRTManager();
    }
}
