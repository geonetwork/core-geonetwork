package org.fao.geonet.kernel.harvest.harvester;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.quartz.*;
import org.springframework.context.ConfigurableApplicationContext;

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
    private Thread _this = null;
    protected Logger log = Log.createLogger(Geonet.HARVESTER);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
          _this = Thread.currentThread();
            harvester.harvest();
        } catch (Throwable t) {
            throw new JobExecutionException(t, false);
        }
    }

    public String getHarvesterId() {
        return harvesterId;
    }
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        ApplicationContextHolder.set(applicationContext);
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
        
        // Following the suggestion of InterruptableJob
        // Sometimes the harvester is frozen
        // give some time, but if it does not finish properly...
        // just kill it!!
         new Thread(){
            @Override
            public void run() {
                super.run();
                
                //Wait for proper shutdown (a minute, more than enough!)
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    log.error(e);
                }

                //Still running?
                if(_this.isAlive()) {
                    //Then kill it!
                    log.error("Forcefully stopping harvester thread '" + 
                            getHarvesterId() + "'.");
                    try {                
                       _this.interrupt();
                    } catch (Throwable e) {
                        log.error(e);
                    }
                }
            }
        }.start();
    }
    
    public Thread getThread() {
      return _this;
    }
}
