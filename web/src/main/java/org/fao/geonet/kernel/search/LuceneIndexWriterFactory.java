package org.fao.geonet.kernel.search;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jeeves.utils.Log;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.index.CategoryDocumentBuilder;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.fao.geonet.constants.Geonet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* Lucene only allows one IndexWriter to be open at a time.
 However, multiple threads can use this single IndexWriter.
 This class manages a global IndexWriter and uses reference counting to
 determine when it can be closed.  */
public class LuceneIndexWriterFactory {

    protected final Map<String, IndexWriter> _writers = new HashMap<String, IndexWriter>();
    protected volatile DirectoryTaxonomyWriter _taxoIndexWriter;
    protected volatile int _count;
    private final File _luceneDir;
    private File _taxoDir;
    private final PerFieldAnalyzerWrapper _analyzer;
    private final LuceneConfig _luceneConfig;

    private final Lock optimizingLock = new ReentrantLock();

    public LuceneIndexWriterFactory( File luceneDir, File taxoDir, PerFieldAnalyzerWrapper analyzer, LuceneConfig luceneConfig ) {
        _luceneDir = luceneDir;
        _taxoDir = taxoDir;
        _analyzer = analyzer;
        _luceneConfig = luceneConfig;
    }
    public synchronized void openWriter() {
        _count++;
        Log.info(Geonet.INDEX_ENGINE, "Opening Index_writer, ref count " + _count);
    }

    public synchronized boolean isOpen() {
        return _count > 0;
    }


    public synchronized void closeWriter() throws Exception {
        Log.info(Geonet.INDEX_ENGINE, "Closing Index_writer, ref _count " + _count);

        // lower reference count, close if count reaches zero
        if (_count > 0) {
            _count--;
            if (_count == 0) {
                commit();
                // Similarly, before a writer close()s the IndexWriter, it must close() the TaxonomyWriter.
                if (_taxoIndexWriter != null) {
                    _taxoIndexWriter.close();
                    _taxoIndexWriter = null;
                }
                for( Map.Entry<String, IndexWriter> entry : _writers.entrySet() ) {
                    String locale = entry.getKey();
                    IndexWriter writer = entry.getValue();
                    Log.info(Geonet.INDEX_ENGINE, "Closing Index_writer, locale: " + locale + " ram in use: " + writer.ramSizeInBytes()
                            + " docs buffered: " + writer.numRamDocs());
                    writer.close();
                }
                _writers.clear();
            } else {
                commit();
            }
        }
    }

    public synchronized void addDocument(String locale, Document doc, List<CategoryPath> categories) throws Exception {
        IndexWriter writer = getWriter(locale);
        // Add taxonomy first
        try {
            CategoryDocumentBuilder categoryDocBuilder = new CategoryDocumentBuilder(_taxoIndexWriter);
            categoryDocBuilder.setCategoryPaths(categories);
            categoryDocBuilder.build(doc);
            
            if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Taxonomy writer: " + _taxoIndexWriter.toString());
                Log.debug(Geonet.INDEX_ENGINE, "Categories:" + categories.size());
//                Log.debug(Geonet.INDEX_ENGINE, "categoryDocBuilder:" + categoryDocBuilder.toString());
//                Log.debug(Geonet.INDEX_ENGINE, "getCacheMemoryUsage:" + _taxoIndexWriter.getCacheMemoryUsage() + ", " + _taxoIndexWriter.getSize());
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
        
        writer.addDocument(doc);
    }

    /**
     * Commit taxonomy and current writers content
     * 
     * @throws Exception
     */
    public synchronized void commit() throws Exception {
        // Before a writer commit()s the IndexWriter, it must commit() the TaxonomyWriter. 
        // Nothing should be added to the index between these two commit()s.
        if (true || isOpen()) {
            if (_taxoIndexWriter != null) {
                try {
                    Log.debug(Geonet.INDEX_ENGINE, "Taxonomy writer: getCacheMemoryUsage:" + _taxoIndexWriter.getCacheMemoryUsage() + ", " + _taxoIndexWriter.getSize());
                    _taxoIndexWriter.commit();
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.debug(Geonet.INDEX_ENGINE, "Failed to commit taxonomy");
                    e.printStackTrace();
                }
            }
            for( IndexWriter writer : openWriters()) {
                writer.commit();
            }
        } else {
            Log.debug(Geonet.INDEX_ENGINE, "Could not commit, is open.");
        }
    }
    
    public synchronized void addDocument( String locale, Document doc ) throws Exception {
        getWriter(locale).addDocument(doc);
    }
    
    public synchronized void deleteDocuments( Term term ) throws Exception {
        for( IndexWriter writer : allExistingWriters() ) {
            writer.deleteDocuments(term);
        }
        // TODO : delete document from taxonomy ?
    }

    public void optimize() throws Exception {
        if (optimizingLock.tryLock()) {
            try {
                openWriter();
                Log.info(Geonet.INDEX_ENGINE, "Optimizing the Lucene Index...");
                for( IndexWriter writer : allExistingWriters() ) {
                    writer.optimize();
                }
                Log.info(Geonet.INDEX_ENGINE, "Optimizing Done.");
            } finally {
                closeWriter();
                optimizingLock.unlock();
            }
        }
    }
    public synchronized void createDefaultLocale() throws IOException {
        File enLocale = new File(_luceneDir, Geonet.DEFAULT_LANGUAGE);
        enLocale.mkdirs();
        IndexWriter writer = new IndexWriter(FSDirectory.open(enLocale), _analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
        writer.close();
    }

    static String normalize( String locale ) {
        if(locale == null) {
            locale = "none";
        }
        return locale;
    }

    // ------------------- Private methods ------------------- //

    private Collection<IndexWriter> allExistingWriters() throws Exception {
        File[] files = _luceneDir.listFiles();
        if (files != null) {
            for( File file : files ) {
                getWriter(file.getName());
            }
        }
        return _writers.values();
    }
    private Collection<IndexWriter> openWriters() throws Exception {    
        return _writers.values();
    }
    private IndexWriter getWriter( String locale ) throws Exception {
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "Getting writer for locale " + locale);
        
        locale = normalize(locale);
        
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "  Normalized locale " + locale);
        
        IndexWriter writer = _writers.get(locale);
        if (writer == null) {
            File indexDir = new File(_luceneDir, locale);
            if (!indexDir.exists() && !indexDir.mkdirs()) {
                throw new Error("Unable to create index directory: " + indexDir);
            }
            
            IndexWriterConfig iwc = new IndexWriterConfig(_luceneConfig.getLuceneVersion(), 
                                                          SearchManager.getAnalyzer(locale, false));
            writer = new IndexWriter(FSDirectory.open(indexDir), iwc);
            //writer = new IndexWriter(FSDirectory.open(indexDir), SearchManager.getAnalyzer(locale, false), IndexWriter.MaxFieldLength.UNLIMITED);
            // TODO : Check deprecated
            writer.setRAMBufferSizeMB(_luceneConfig.getRAMBufferSize());
            //writer.setMergeFactor(_luceneConfig.getMergeFactor());
            _writers.put(locale, writer);
        }
        
        if (_taxoIndexWriter == null) {
            if(Log.isDebugEnabled(Geonet.LUCENE)) {
                Log.debug(Geonet.INDEX_ENGINE, "Null taxonomy writer, creating it from directory " + _taxoDir.getPath());
            }
            try {
                _taxoIndexWriter = new DirectoryTaxonomyWriter(FSDirectory.open(_taxoDir));
            } catch (CorruptIndexException e) {
                e.printStackTrace();
            } catch (LockObtainFailedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
        if(Log.isDebugEnabled(Geonet.LUCENE)) {
           Log.debug(Geonet.INDEX_ENGINE, "Opening index writer with locale: " + locale 
                                          + ". Opened: " + _count 
                                          + ". RAM in use: " + writer.ramSizeInBytes()
                                          + ". Docs buffered: " + writer.numRamDocs());
        }
        return writer;
    }

}