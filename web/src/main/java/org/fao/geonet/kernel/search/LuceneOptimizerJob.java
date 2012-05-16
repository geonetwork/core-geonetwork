/**
 * 
 */
package org.fao.geonet.kernel.search;

import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
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

	LuceneIndexWriterFactory indexWriter;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			indexWriter.optimize();
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Optimize task failed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setIndexWriter(LuceneIndexWriterFactory indexWriter) {
		this.indexWriter = indexWriter;
	}
	
}
