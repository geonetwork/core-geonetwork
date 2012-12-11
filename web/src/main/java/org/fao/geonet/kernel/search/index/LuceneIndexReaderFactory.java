package org.fao.geonet.kernel.search.index;

import java.io.IOException;

import org.apache.lucene.index.MultiReader;
import org.fao.geonet.kernel.search.spatial.Pair;

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
     * @param priorityLocale if non-null and there the locale exists this locale will be the first
     *        sub-index reader. (normally resulting in its results being the first to be processed)
     * @return an index reader for reading from all indices
     */
    public Pair<Long, GeonetworkMultiReader> aquire(long versionToken) throws IOException {
        return tracker.aquire(versionToken);
    }

    public void release (GeonetworkMultiReader reader) throws InterruptedException, IOException {
        reader.releaseToNRTManager();
    }
}
