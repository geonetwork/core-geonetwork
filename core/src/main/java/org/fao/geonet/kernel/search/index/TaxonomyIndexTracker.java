package org.fao.geonet.kernel.search.index;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.store.Directory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * For concurrency issues this class should not escape the confines of this package because
 * {@link LuceneIndexLanguageTracker} controls access to it and also controls concurrency and 
 * synchronization
 * 
 * @author jeichar
 */
class TaxonomyIndexTracker {
    private DirectoryTaxonomyWriter taxonomyWriter;
    private TaxonomyReader taxonomyReader;
    private LinkedList<TaxonomyReader> expiredReaders = new LinkedList<TaxonomyReader>();
    private final DirectoryFactory taxonomyDir;
    private final LuceneConfig luceneConfig;
    private Directory cachedFSDir;

    public TaxonomyIndexTracker(DirectoryFactory taxonomyDir, LuceneConfig luceneConfig) throws Exception {
        this.taxonomyDir = taxonomyDir;
        this.luceneConfig = luceneConfig;
        init();
    }

    private void init() throws Exception {
		try {
            cachedFSDir = taxonomyDir.createTaxonomyDirectory(luceneConfig);
			this.taxonomyWriter = new DirectoryTaxonomyWriter(cachedFSDir);
			taxonomyWriter.commit(); // create index if not existing yet

			this.taxonomyReader = null;
			try {
				acquire(); // just check the validity of the index
			} finally {
				IOUtils.closeQuietly(this.taxonomyReader);
				this.taxonomyReader = null;
			}
		} catch (Exception e) {
    		Log.error(Geonet.INDEX_ENGINE, "An error occurred while openning taxonomy readers/writers", e);
    		ArrayList<Throwable> errors = new ArrayList<Throwable>();
			close(errors);
            if (!errors.isEmpty()) {
                for (Throwable throwable : errors) {
                    Log.error(Geonet.LUCENE, "Failure while closing luceneIndexLanguageTracker", throwable);
                }
            }
    		throw e;
    	}
    }
    
    TaxonomyReader acquire() throws IOException {
        if(taxonomyReader == null) {
            this.taxonomyReader = new DirectoryTaxonomyReader(taxonomyWriter);
        }

        for (Iterator<TaxonomyReader> iterator = expiredReaders.iterator(); iterator.hasNext();) {
            TaxonomyReader reader = iterator.next();
            if(reader.getRefCount() < 1) {
                IOUtils.closeQuietly(reader);
                iterator.remove();
            }
        }

        return taxonomyReader;
    }


    Document addDocument(Document doc, Collection<CategoryPath> categories) {
        Document docAfterFacetBuild = null;
        try {
            docAfterFacetBuild = luceneConfig.getTaxonomyConfiguration().build(taxonomyWriter, doc);
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Taxonomy writer: " + taxonomyWriter.toString());
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
        return docAfterFacetBuild;
    }

    void close(List<Throwable> errors) throws IOException {
        try {
            if(taxonomyReader != null) {
                taxonomyReader.close();
                taxonomyReader = null;
            }
        } catch (Throwable e) {
            errors.add(e);
        }

        for (Iterator<TaxonomyReader> iterator = expiredReaders.iterator(); iterator.hasNext();) {
            TaxonomyReader reader = iterator.next();
            IOUtils.closeQuietly(reader);
        }
        expiredReaders.clear();

        try {
            if (taxonomyWriter != null) {
                taxonomyWriter.close();
                taxonomyWriter = null;
            }
        } catch (Throwable e) {
            errors.add(e);
        }
        try {
            if (cachedFSDir != null) {
                cachedFSDir.close();
                cachedFSDir = null;
            }
        } catch (Throwable e) {
            errors.add(e);
        }
    }

    
    void reset() throws Exception {
        List<Throwable> errors = new ArrayList<Throwable>(5);
        close(errors);

        taxonomyDir.resetTaxonomy();
        init();

        if(!errors.isEmpty()) {
            for (Throwable throwable : errors) {
                Log.error(Geonet.LUCENE, "Failure while closing luceneIndexLanguageTracker", throwable);
            }
        }
    }

    void commit() {
        try {
            try {
                taxonomyWriter.commit();
            } catch (Throwable e) {
                Log.error(Geonet.LUCENE, "Error committing taxonomy: "+taxonomyWriter, e);
            }
        } catch (OutOfMemoryError e) {
            try {
                Log.error(Geonet.LUCENE, "OOM Error committing taxonomy: "+taxonomyWriter, e);
                reset();
            } catch (Exception e1) {
                Log.error(Geonet.LUCENE, "Error resetting lucene indices", e);
            }
            throw new RuntimeException(e);
        }
    }
    TaxonomyWriter writer() {
        return taxonomyWriter;
    }

    public void maybeRefresh() throws IOException {
        // do nothing for now
        if (taxonomyReader != null) {
            TaxonomyReader newReader = TaxonomyReader.openIfChanged(taxonomyReader);
            if (newReader != null) {
                if (taxonomyReader.getRefCount() == 0) {
                    IOUtils.closeQuietly(taxonomyReader);
                } else {
                    expiredReaders.add(taxonomyReader);
                }
                taxonomyReader = newReader;
            }
        }
    }
}
