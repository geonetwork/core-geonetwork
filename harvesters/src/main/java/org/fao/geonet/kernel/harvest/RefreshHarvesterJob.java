package org.fao.geonet.kernel.harvest;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

public class RefreshHarvesterJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            HarvestManagerImpl harvestManager = (HarvestManagerImpl) schedulerContext.get("harvest-manager");
            if (harvestManager != null) {
                harvestManager.refreshHarvesters();
            }
        } catch (SchedulerException e) {
            Log.error(Geonet.HARVEST_MAN, e.getMessage());
            e.printStackTrace();
        }
    }
}
