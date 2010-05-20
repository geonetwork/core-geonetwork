package org.fao.geonet.kernel.search;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jeeves.utils.Log;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import org.fao.geonet.constants.Geonet;

/* Lucene only allows one IndexWriter to be open at a time.  
   However, multiple threads can use this single IndexWriter.  
   This class manages a global IndexWriter and uses reference counting to 
   determine when it can be closed.  */

public class LuceneIndexWriterFactory {
	
	protected IndexWriter _writer;
	protected int _count;
	private File _luceneDir;
	private PerFieldAnalyzerWrapper _analyzer;
		
	// true iff optimization is in progress
	private boolean _optimizing = false; 
	private Object  _mutex = new Object(); 

	
	public LuceneIndexWriterFactory(File luceneDir, PerFieldAnalyzerWrapper analyzer) {
		_luceneDir = luceneDir;
		_analyzer = analyzer;
	}

	public synchronized void openWriter() throws Exception {
		if (_count == 0) {
			_writer = new IndexWriter(FSDirectory.open(_luceneDir), _analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			_writer.setRAMBufferSizeMB(48.0d); 
			// 48MB seems to be plenty for running at least two long 
			// indexing jobs (eg. importing 20,000 records) and keeping disk 
			// activity for lucene index writing to a minimum - should be a config 
			// option
		}
		_count++;
		Log.info(Geonet.INDEX_ENGINE, "Opening Index_writer, ref count "+_count+" ram in use "+_writer.ramSizeInBytes()+" docs buffered "+_writer.numRamDocs());
	}

	public synchronized boolean isOpen() {
		if (_count > 0) return true;
		else return false;
	}

	public synchronized void closeWriter() throws Exception {
		
		// lower reference count, close if count reaches zero
		if (_count > 0) {
			_count--;
			Log.info(Geonet.INDEX_ENGINE, "Closing Index_writer, ref _count "+_count+" ram in use "+_writer.ramSizeInBytes()+" docs buffered "+_writer.numRamDocs());
			if (_count==0) _writer.close(); 
		}
	}

	public void addDocument(Document doc) throws Exception {
		_writer.addDocument(doc);
	}

	public void deleteDocuments(Term term) throws Exception {
		_writer.deleteDocuments(term);
	}
	
	public void optimize() throws Exception {
		if (_optimizing) return;
		synchronized (_mutex) {
 			_optimizing  = true;
			Log.info(Geonet.INDEX_ENGINE,"Optimizing the Lucene Index...");
			_writer.optimize(); 
			Log.info(Geonet.INDEX_ENGINE,"Optimizing Done.");
			_optimizing = false;
		}
		return;
	}


	
}
