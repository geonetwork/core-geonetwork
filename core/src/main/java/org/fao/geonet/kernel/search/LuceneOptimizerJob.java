/**
 * 
 */
package org.fao.geonet.kernel.search;

import org.fao.geonet.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Performs the LuceneOptimization task.  
 * 
 * @author jeichar
 */
@DisallowConcurrentExecution
public class LuceneOptimizerJob implements Job {

    LuceneIndexLanguageTracker indexTracker;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
		    indexTracker.optimize();
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Optimize task failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setIndexTracker(LuceneIndexLanguageTracker indexTracker) {
		this.indexTracker = indexTracker;
	}
	
}
