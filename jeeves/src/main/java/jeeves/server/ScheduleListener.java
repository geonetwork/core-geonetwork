package jeeves.server;

import jeeves.server.context.ScheduleContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Put the ScheduleContext and Schedule objects in JobExecutionContext so that
 * ScheduleJob can access them.
 * 
 * This is necessary since Schedule objects could contain state (even if it is
 * bad). So the same Schedule object must be used. Also the ScheduleContext
 * object is required by ScheduleContext for executing the schedule.
 * 
 * @author jeichar
 */
public class ScheduleListener implements JobListener {

    private final ScheduleManager scheduleManager;

    public ScheduleListener(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    public String getName() {
        return "ScheduleJob configuration listener";
    }

    public void jobToBeExecuted(JobExecutionContext context) {
        if (context.getJobInstance() instanceof ScheduleJob) {
            ScheduleJob scheduleJob = (ScheduleJob) context.getJobInstance();

            ScheduleInfo info = scheduleManager.getScheduleInfo(scheduleJob.getScheduleName());
            ScheduleContext scheduleContext = new ScheduleContext(info.name, scheduleManager.getApplicationContext(),
                scheduleManager.getHtContexts(), scheduleManager.getEntityManager());
            scheduleContext.setBaseUrl(scheduleManager.getBaseUrl());
            scheduleContext.setAppPath(scheduleManager.getAppPath());

            scheduleJob.setSchedule(info.schedule);
            scheduleJob.setScheduleContext(scheduleContext);
        }
    }

    public void jobExecutionVetoed(JobExecutionContext context) {
        // no action
    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        // no action
    }

}
