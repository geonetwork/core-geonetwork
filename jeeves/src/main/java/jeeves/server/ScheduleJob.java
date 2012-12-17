package jeeves.server;

import jeeves.exceptions.JeevesException;
import jeeves.interfaces.Schedule;
import jeeves.server.context.ScheduleContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A Quartz job that executes a Schedule instance.
 * 
 * @author jeichar
 */
public class ScheduleJob implements Job {

    public static final String NAME_FIELD_NAME = "scheduleName";
    
    private String scheduleName;
    private Schedule schedule;
    private ScheduleContext scheduleContext;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            schedule.exec(scheduleContext);

            scheduleContext.getResourceManager().close();
            return;
        } catch (JeevesException e) {
            error("Communication exception while executing schedule : " + scheduleContext.getScheduleName());
            error(" (C) Status  : " + e.getId());
            error(" (C) Message : " + e.getMessage());

            if (e.getObject() != null)
                error(" (C) Object  : " + e.getObject());
        }

        catch (Exception e) {
            error("Raised exception when executing schedule : " + scheduleContext.getScheduleName());
            error(" (C) Stack trace : " + Util.getStackTrace(e));
        }

        // --- in case of exception we have to abort all resources

        abort(scheduleContext);

    }

    // --------------------------------------------------------------------------

    private void abort(ScheduleContext context) {
        try {
            context.getResourceManager().abort();
        } catch (Exception ex) {
            error("CANNOT ABORT PREVIOUS EXCEPTION");
            error(" (C) Exc : " + ex);
        }
    }

    private void error(String message) {
        Log.error(Log.SCHEDULER, message);
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setScheduleContext(ScheduleContext scheduleContext) {
        this.scheduleContext = scheduleContext;
    }

    public String getScheduleName() {
        return scheduleName;
    }
    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

}
