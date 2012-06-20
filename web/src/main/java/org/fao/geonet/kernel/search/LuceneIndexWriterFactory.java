package org.fao.geonet.kernel.search;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jeeves.utils.Log;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
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
    protected volatile int _count;
    private final File _luceneDir;
    private final PerFieldAnalyzerWrapper _analyzer;
    private final LuceneConfig _luceneConfig;

    private final Lock optimizingLock = new ReentrantLock();

    public LuceneIndexWriterFactory( File luceneDir, PerFieldAnalyzerWrapper analyzer, LuceneConfig luceneConfig ) {
        _luceneDir = luceneDir;
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

    public synchronized void commit() throws Exception {
        if (isOpen()) {
            for( IndexWriter writer : openWriters()) {
                writer.commit();
            }

        }
    }

    public synchronized void addDocument( String locale, Document doc ) throws Exception {
        getWriter(locale).addDocument(doc);
    }

    public synchronized void deleteDocuments( Term term ) throws Exception {
        for( IndexWriter writer : allExistingWriters() ) {
            writer.deleteDocuments(term);
        }
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
            Log.debug(Geonet.LUCENE, "getting writer for locale " + locale);
        locale = normalize(locale);
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "normalized locale " + locale);
        IndexWriter writer = _writers.get(locale);
        if (writer == null) {
            File indexDir = new File(_luceneDir, locale);
            if (!indexDir.exists() && !indexDir.mkdirs()) {
                throw new Error("Unable to create index directory: " + indexDir);
            }
            
            writer = new IndexWriter(FSDirectory.open(indexDir), SearchManager.getAnalyzer(locale, false), IndexWriter.MaxFieldLength.UNLIMITED);
            writer.setRAMBufferSizeMB(_luceneConfig.getRAMBufferSize());
            writer.setMergeFactor(_luceneConfig.getMergeFactor());
            _writers.put(locale, writer);
        }
        Log.info(Geonet.INDEX_ENGINE, "Opening Index_writer, locale: " + _count + " ram in use: " + writer.ramSizeInBytes()
                + " docs buffered: " + writer.numRamDocs());
        return writer;
    }

}