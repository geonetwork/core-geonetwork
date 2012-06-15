package org.fao.geonet.kernel.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jeeves.utils.Log;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.lucene.LuceneTaxonomyReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.constants.Geonet;

/**
 * Utility class to get/refresh readers for SearchManager class Works by opening an IndexReader at
 * startup and keeping that reader open. It is never closed. Users of this class call getReader
 * which increments the ref count on the IndexReader when they need an IndexReader and when
 * finished, releaseReader which decrements the ref count. Any call to getReader may reopen the
 * index if changes have been made. Idea and some code taken from SearchManager.java, Lucene In
 * Action 2, Manning Books
 */

public class LuceneIndexReaderFactory {
    private static final long LOCK_WAIT_TIME = 15;
    private static final TimeUnit WAIT_UNIT = TimeUnit.SECONDS;
    private Map<String/* locale */, IndexReader> subReaders = new HashMap<String, IndexReader>();
    private volatile TaxonomyReader currentTaxoReader;
    private File luceneDir;
    private File taxoDir;
    private final Lock lock = new ReentrantLock();
    // ===========================================================================
    // Constructor

    public LuceneIndexReaderFactory( File dir ) throws IOException {
        this.luceneDir = dir;
    }

  public LuceneIndexReaderFactory(File dir, File taxoDir) throws IOException {
      this.luceneDir = dir;
      this.taxoDir = taxoDir;
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
    public IndexReader getReader( String priorityLocale ) throws InterruptedException, IOException {
        priorityLocale = LuceneIndexWriterFactory.normalize(priorityLocale);

        lock.tryLock(LOCK_WAIT_TIME, WAIT_UNIT);
        try {
            maybeReopen();
            LinkedList<IndexReader> orderedReaders = new LinkedList<IndexReader>();
            for( Map.Entry<String, IndexReader> entry : subReaders.entrySet() ) {
                IndexReader reader = entry.getValue();
                if (entry.getKey().equalsIgnoreCase(priorityLocale)) {
                    orderedReaders.add(0, reader);
                } else {
                    orderedReaders.add(reader);
                }
                reader.incRef();
            }
            return new MultiReader(orderedReaders.toArray(new IndexReader[orderedReaders.size()]), false);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Refresh and return the current taxonomy reader.
     * 
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public TaxonomyReader getTaxonomyReader() {
        if (currentTaxoReader == null)
            try {
                currentTaxoReader = new LuceneTaxonomyReader(
                        FSDirectory.open(taxoDir));
            } catch (CorruptIndexException e) {
                Log.warning(Geonet.SEARCH_ENGINE, "Taxonomy index is corrupted. Error is " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.warning(Geonet.SEARCH_ENGINE, "Failed to open taxonomy reader from directory " 
                    + taxoDir.getPath() + ". Error is " + e.getMessage());
                e.printStackTrace();
            }
        return currentTaxoReader;
    }

    public void releaseReader( IndexReader reader ) throws InterruptedException, IOException {
        lock.tryLock(LOCK_WAIT_TIME, WAIT_UNIT);
        try {
            MultiReader multiReader = (MultiReader) reader;
            reader.close();  // multireader increments reader so close it so the readers will be correctly decremented
            for( IndexReader r : multiReader.getSequentialSubReaders() ) {
                r.decRef();
            }
        } finally {
            lock.unlock();
        }
    }
    public void close() throws InterruptedException, IOException {
        lock.tryLock(LOCK_WAIT_TIME, WAIT_UNIT);
        try {
            for( IndexReader r : subReaders.values() ) {
                while( r.getRefCount() > 1 )
                    r.decRef();
                r.close();
            }
        } finally {
            lock.unlock();
        }
    }
    public boolean isUpToDateReader( IndexReader reader ) throws InterruptedException, IOException {
        lock.tryLock(LOCK_WAIT_TIME, WAIT_UNIT);
        try {
            maybeReopen();

            MultiReader multiReader = (MultiReader) reader;
            LinkedList<IndexReader> otherReaders = new LinkedList<IndexReader>(Arrays.asList(multiReader.getSequentialSubReaders()));
            boolean sameNumReaders = otherReaders.size() == subReaders.size();

            for( IndexReader indexReader : subReaders.values() ) {
                otherReaders.remove(indexReader);
            }
            return sameNumReaders && otherReaders.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    // ===========================================================================
    // Private Methods

    private Set<String> listIndices() {
        Set<String> indices = new HashSet<String>();
        final File[] files = luceneDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (new File(file, "segments.gen").exists()) {
                    indices.add(file.getName());
                }
            }
        }
        return indices;
    }

    private void maybeReopen() throws InterruptedException, IOException {
        Set<String> indices = listIndices();

        if(indices.isEmpty()) throw new AssertionError("No lucene indices exist.  Need to regenerate");

        for( String indexDirName : indices ) {
            IndexReader reader = subReaders.get(indexDirName);
            if(reader == null) {
                FSDirectory directory = FSDirectory.open(new File(luceneDir,indexDirName));
                reader = IndexReader.open(directory, true);
                subReaders.put(indexDirName, reader);
            } else {
                final IndexReader newReader = reader.reopen();
                if (newReader != reader) {
                    if(reader.getRefCount() > 0) {
                        reader.decRef();    // it will be closed when refCount is 0 - this could
                                            // be here or when other threads using it finish
                                            // and call releaseReader
                    }
                    subReaders.put(indexDirName, newReader);
                    if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                        Log.debug(Geonet.SEARCH_ENGINE, "Thread " + Thread.currentThread().getId() + ": reopened IndexReader");
                }
            }
        }
    }
}
