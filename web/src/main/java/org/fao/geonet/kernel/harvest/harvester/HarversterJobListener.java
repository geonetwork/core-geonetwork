package org.fao.geonet.kernel.harvest.harvester;

import org.fao.geonet.kernel.harvest.HarvestManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Put the AbstractHarvester in JobExecutionContext so that HarvesterJob can access them. 
 * 
 * This is necessary since Harvester objects could contain state (even if it is bad).  So the same Harvester object
 * must be used.
 * 
 * @author jeichar
 */
public class HarversterJobListener implements JobListener {

    private static final String HARVESTER_JOB_CONFIGURATION_LISTENER = "Harvester Job configuration listener";
    
    private HarvestManager manager;

    public HarversterJobListener(HarvestManager manager) {
       this.manager = manager;
    }

    @Override
    public String getName() {
        return  HARVESTER_JOB_CONFIGURATION_LISTENER;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        if (context.getJobInstance() instanceof HarvesterJob) {
            HarvesterJob harvesterJob = (HarvesterJob) context.getJobInstance();
            AbstractHarvester harvester = manager.getHarvester(harvesterJob.getHarvesterId());
            harvesterJob.setHarvester(harvester);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        //nothing
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        // nothing

    }

}
