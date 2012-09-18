package org.fao.geonet.kernel.harvest.harvester;

import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.harvest.HarvestMessage;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
public class HarvesterJob implements Job {
    
    public static final String ID_FIELD = "harvesterId";
    String harvesterId;
    AbstractHarvester harvester;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {

            if(ClusterConfig.isEnabled()) {
                if (harvester.getNodeId().equals(ClusterConfig.getClientID())) {
                    try {
                        Log.info(Geonet.HARVESTER, "clustering enabled, creating harvest message");
                        HarvestMessage message = new HarvestMessage();
                        message.setId(harvester.getID());
                        Producer harvestProducer = ClusterConfig.get(Geonet.ClusterMessageQueue.HARVEST);
                        harvestProducer.produce(message);
                    }
                    catch (ClusterException x) {
                        Log.error(Geonet.HARVESTER, x.getMessage());
                        x.printStackTrace();
                    }
                }
            } else {
            harvester.harvest();
            }

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

    public void setHarvester(AbstractHarvester harvester) {
        this.harvester = harvester;
    }

    
}
