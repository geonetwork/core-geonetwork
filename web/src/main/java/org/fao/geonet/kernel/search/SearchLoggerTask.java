package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.fao.geonet.kernel.search.log.SearcherLogger;

/**
 * Task to launch a new thread for search logging.
 *
 * Other idea: Another approach could be to use JMS, to send an
	 * asynchronous message with search info in order to log them.
 *
 * @author francois
 */
public class SearchLoggerTask implements Runnable {
    private ServiceContext srvContext;
    boolean logSpatialObject;
    String luceneTermsToExclude;
	Query query;
	int numHits;
	Sort sort;
	String geomWKT;
	String value;


    public SearchLoggerTask(ServiceContext srvContext,
			boolean logSpatialObject, String luceneTermsToExclude,
			Query query, int numHits, Sort sort, String geomWKT,
			String value) {
    			this.srvContext = srvContext;
    			this.logSpatialObject = logSpatialObject;
    			this.luceneTermsToExclude = luceneTermsToExclude;
    			this.query = query;
    			this.numHits = numHits;
    			this.sort = sort;
    			this.geomWKT = geomWKT;
    			this.value = value;
	}

	public void run() {
	    if(!ServiceManager.searchLoggingEnabled()) return;

        try {
        	SearcherLogger searchLogger = new SearcherLogger(srvContext, logSpatialObject, luceneTermsToExclude);
    		searchLogger.logSearch(query, numHits, sort, geomWKT, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}