package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.constants.Geonet;

import java.io.File;
import java.io.IOException;

/** Utility class to get/refresh readers for SearchManager class
		Works by opening an IndexReader at startup and keeping that reader open. 
		It is never closed. Users of this class call getReader which increments
		the ref count on the IndexReader when they need an IndexReader and 
		when finished, releaseReader which decrements the ref count. 
		Any call to getReader may reopen the index if changes have been made.

    Idea and some code taken from SearchManager.java, Lucene In Action 2, 
		                                                  Manning Books */

public class LuceneIndexReaderFactory {

  private IndexReader currentReader;
  private boolean reopening;
	private Object mutex = new Object(); // for debugging

	//===========================================================================
	// Constructor

  public LuceneIndexReaderFactory(File dir) throws IOException {
    currentReader = IndexReader.open(FSDirectory.open(dir), true);
    warm(currentReader);
	}


	//===========================================================================
	// Public interface methods

  public IndexReader getReader() throws InterruptedException, IOException {
		maybeReopen();
		synchronized (this) {
    	currentReader.incRef();
		}
    return currentReader;
  }    

  public synchronized void releaseReader(IndexReader reader) throws IOException {
      reader.decRef();
  }

	//===========================================================================
	// Private Methods

  private void warm(IndexReader reader) {}                                

  private synchronized void startReopen() throws InterruptedException {
    while (reopening) {
			Log.debug(Geonet.SEARCH_ENGINE, "Thread "+Thread.currentThread().getId()+": Waiting whilst IndexReader reopen completes..");
      wait(); // wait for notifyAll in doneReopen to tell us that 
			        // current reopen finished
			Log.debug(Geonet.SEARCH_ENGINE, "Thread "+Thread.currentThread().getId()+": Continuing after wait");
    }
    reopening = true;
  }

  private synchronized void finishReopen() {
    reopening = false;
    notifyAll(); // tell any thread waiting in startReopen to 
		             // continue 
  }

  private void maybeReopen() throws InterruptedException, IOException { 

    startReopen();
    try {
       IndexReader newReader = currentReader.reopen(); 
       if (newReader != currentReader) {
				warm(newReader);
       	swapReader(newReader);
				Log.debug(Geonet.SEARCH_ENGINE, "Thread "+Thread.currentThread().getId()+": reopened IndexReader");
			}
		} finally {
      finishReopen();
    }
  }

  private synchronized void swapReader(IndexReader newReader) throws IOException {
		currentReader.decRef(); // it will be closed when refCount is 0 - this could
														// be here or when other threads using it finish
														// and call releaseReader
    currentReader = newReader;
  }
}
