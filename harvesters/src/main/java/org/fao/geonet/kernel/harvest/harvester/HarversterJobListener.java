package org.fao.geonet.kernel.harvest.harvester;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.kernel.harvest.HarvestManager;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Put the AbstractHarvester in JobExecutionContext so that HarvesterJob can access them.
 * 
 * This is necessary since Harvester objects could contain state (even if it is bad). So the same Harvester object must be used.
 * 
 * @author jeichar
 */
public class HarversterJobListener implements JobListener {

    private static final String HARVESTER_JOB_CONFIGURATION_LISTENER = "Harvester Job configuration listener";

    private List<HarvestManager> listOfManager = new ArrayList<HarvestManager>();

    private static HarversterJobListener INSTANCE = new HarversterJobListener() {
    };

    public static HarversterJobListener getInstance(HarvestManager manager) {
        INSTANCE.listOfManager.add(manager);
        return INSTANCE;
    }

    @Override
    public String getName() {
        return HARVESTER_JOB_CONFIGURATION_LISTENER;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        if (context.getJobInstance() instanceof HarvesterJob) {

            HarvesterJob harvesterJob = (HarvesterJob) context.getJobInstance();
            ConfigurableApplicationContext applicationContext = null;
            AbstractHarvester<?> harvester = null;
            for (HarvestManager manager : listOfManager) {
                harvester = manager.getHarvester(harvesterJob.getHarvesterId());
                if (harvester != null) {
                    applicationContext = manager.getApplicationContext();
                    // Harvester found
                    break;
                }
            }
            // if(harvester == null) {
            // // Null harvester should not happen
            // }

            harvesterJob.setHarvester(harvester);
            harvesterJob.setApplicationContext(applicationContext);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // nothing
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        // nothing

    }

}
