package org.fao.geonet.kernel.search;

import jeeves.utils.Log;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.SearcherLifetimeManager;
import org.apache.lucene.search.SearcherLifetimeManager.PruneByAge;
import org.apache.lucene.store.FSDirectory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/** Utility class to get/refresh readers for SearchManager class
	*	Works using the Lucene SearcherManager which uses one IndexSearcher and  
	*	keeps that searcher open - life of the IndexSearcher is then controlled 
	* by SearchLifetimeManager. 
  */

public class LuceneIndexReaderFactory {

  private SearcherManager searcherManager;
	private SearcherLifetimeManager mgr = new SearcherLifetimeManager();

	//===========================================================================
	// Constructor

  public LuceneIndexReaderFactory(File dir) throws IOException {
		searcherManager = new SearcherManager(FSDirectory.open(dir), null);
		Timer t = new Timer("IndexSearcher pruner/refresher", true);
		t.schedule(new PruneRefreshTask(mgr, searcherManager), 1000L, 10000L);
	}

	//===========================================================================
	// Public interface methods

  public Pair<Long,IndexSearcher> getReader(long token) throws IOException, InterruptedException {
		
		Log.debug(Geonet.INDEX_ENGINE, "Looking for index searcher with token "+token);
		IndexSearcher is = mgr.acquire(token);
		if (is != null) {
			Log.debug(Geonet.INDEX_ENGINE, "		- Retrieved index searcher with token "+token);
			return Pair.read(token, is);
		} else {
			// while (!searcherManager.maybeRefresh()) Thread.sleep(10); - no need to
			// wait for a refresh - let it happen in the background
			is = searcherManager.acquire();
			long newToken;
			synchronized (this) {
				newToken = mgr.record(is);
			}
			Log.debug(Geonet.INDEX_ENGINE, "		- Created index searcher with token "+newToken);
			return Pair.read(newToken, is);
		}
  }    

	//===========================================================================

  public synchronized void releaseReader(IndexSearcher is) throws IOException {
		mgr.release(is);
		is = null;
  }

	//===========================================================================
	// Thread to prune or refresh IndexSearchers
	class PruneRefreshTask extends TimerTask {
		SearcherLifetimeManager mgr;
		SearcherManager searcherManager;

		PruneRefreshTask(SearcherLifetimeManager mgr, SearcherManager searcherManager) {
			this.mgr = mgr;
			this.searcherManager = searcherManager;
		}

		public void run() {
			try {
				// IndexSearcher/Reader must be 1 hour old and in need of refresh to be
				// removed
				mgr.prune(new PruneByAge(3600.0)); 
				searcherManager.maybeRefresh();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
	}
}
