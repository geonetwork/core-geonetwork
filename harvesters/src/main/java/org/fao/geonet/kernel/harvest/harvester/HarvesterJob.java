package org.fao.geonet.kernel.harvest.harvester;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * A Quartz job that obtains the Harvester from the JobExecutionContext and executes it.
 * 
 * In Quartz a job is stateless so they can be scaled.  This is not useful for
 * Geonetwork because the harvester needs certain state like ServiceContext.  So instead a job 
 * is submitted to the Scheduler and at the same time a lister is added that listens for that Job.
 * to be executed.  When the Job is to be executed the listener puts the Harvester
 * on the JobExecutionContext.  This Job obtains the harvester from the context and executes it.
 * 
 * @author jeichar
 */
@DisallowConcurrentExecution
public class HarvesterJob implements Job, InterruptableJob {
    
    public static final String ID_FIELD = "harvesterId";
    String harvesterId;
    AbstractHarvester<?> harvester;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            harvester.harvest();

        } catch (Throwable t) {
            throw new JobExecutionException(t, false);
        }
    }

    public String getHarvesterId() {
        return harvesterId;
    }

    public void setHarvesterId(String harvesterId) {
        this.harvesterId = harvesterId;
    }

    public void setHarvester(AbstractHarvester<?> harvester) {
        this.harvester = harvester;
    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {
        harvester.cancelMonitor.set(true);
    }
}
