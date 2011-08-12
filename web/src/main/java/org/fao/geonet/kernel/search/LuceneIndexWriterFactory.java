package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.constants.Geonet;

import java.io.File;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* Lucene only allows one IndexWriter to be open at a time.  
   However, multiple threads can use this single IndexWriter.  
   This class manages a global IndexWriter and uses reference counting to 
   determine when it can be closed.  */

public class LuceneIndexWriterFactory {
	
	protected volatile IndexWriter _writer;
	protected int _count;
	private File _luceneDir;
	private PerFieldAnalyzerWrapper _analyzer;
	private LuceneConfig _luceneConfig;
	
	private final Lock optimizingLock = new ReentrantLock();
	
	public LuceneIndexWriterFactory(File luceneDir, PerFieldAnalyzerWrapper analyzer, LuceneConfig luceneConfig) {
		_luceneDir = luceneDir;
		_analyzer = analyzer;
		_luceneConfig = luceneConfig;
	}

	public synchronized void openWriter() throws Exception {
		if (_count == 0) {
			_writer = new IndexWriter(FSDirectory.open(_luceneDir), _analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			_writer.setRAMBufferSizeMB(_luceneConfig.getRAMBufferSize());
			_writer.setMergeFactor(_luceneConfig.getMergeFactor());
		}
		_count++;
		Log.info(Geonet.INDEX_ENGINE, "Opening Index_writer, ref count " + _count + " ram in use " 
				+ _writer.ramSizeInBytes() + " docs buffered "
				+ _writer.numRamDocs());
	}

	public synchronized boolean isOpen() {
        return _count > 0;
	}

	public synchronized void closeWriter() throws Exception {
		
		// lower reference count, close if count reaches zero
		if (_count > 0) {
			_count--;
			Log.info(Geonet.INDEX_ENGINE, "Closing Index_writer, ref _count "+_count+" ram in use "+_writer.ramSizeInBytes()+" docs buffered "+_writer.numRamDocs());
			if (_count==0) _writer.close(); 
			else _writer.commit();
		}
	}

	public synchronized void commit() throws Exception {
		if (isOpen()) _writer.commit();
	}
		
	public void addDocument(Document doc) throws Exception {
		_writer.addDocument(doc);
	}

	public void deleteDocuments(Term term) throws Exception {
		_writer.deleteDocuments(term);
	}
	
	public void optimize() throws Exception {
		if (optimizingLock.tryLock()) {
			try {
				Log.info(Geonet.INDEX_ENGINE,"Optimizing the Lucene Index...");
				_writer.optimize(); 
				Log.info(Geonet.INDEX_ENGINE,"Optimizing Done.");
			} finally {
				optimizingLock.unlock();
			}
		}
	}


	
}
