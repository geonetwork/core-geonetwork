package org.fao.geonet.kernel.search;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import jeeves.utils.Log;
import jeeves.utils.QuartzSchedulerUtils;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * Handles Optimizing the Lucene index on a scheduled basis
 *  
 * @author jeichar
 */
public class LuceneOptimizerManager {

	private static final String SCHEDULER_ID = "luceneOptimize";
	private final String instanceID = SCHEDULER_ID+"-"+UUID.randomUUID().toString();
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private final Scheduler scheduler;
	private Calendar _beginAt;
	private int _intervalInMinutes;
	private TriggerKey triggerKey;
	private JobDetail jobDetail;
	
	public LuceneOptimizerManager(SearchManager manager, SettingInfo settingsInfo) throws SchedulerException {
		this.scheduler = QuartzSchedulerUtils.getScheduler(SCHEDULER_ID, true);
		this.scheduler.getListenerManager().addJobListener(new Listener(manager), jobGroupEquals(instanceID));
		this.jobDetail = newJob(LuceneOptimizerJob.class).withIdentity(instanceID, instanceID).storeDurably().build();
		if (settingsInfo.getLuceneIndexOptimizerSchedulerEnabled()) {
            _beginAt  = settingsInfo.getLuceneIndexOptimizerSchedulerAt();
            _intervalInMinutes = settingsInfo.getLuceneIndexOptimizerSchedulerInterval();
            scheduleJob();
		} else {
            Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
        }
		
	}

	private void scheduleJob() throws SchedulerException {
		scheduler.unscheduleJob(triggerKey);
		Trigger trigger = newTrigger().withIdentity(instanceID, instanceID).
				withSchedule(simpleSchedule().
				withIntervalInMinutes(_intervalInMinutes).
				repeatForever().
				withMisfireHandlingInstructionFireNow()).
				startAt(getBeginAt()).build();
		this.triggerKey = trigger.getKey();
		scheduler.scheduleJob(this.jobDetail, trigger);
	}

	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	public void reschedule(Calendar beginAt, int interval) throws SchedulerException {
		if (_dateFormat.format(beginAt.getTime()).equals(_dateFormat.format(_beginAt.getTime())) &&
                (interval == _intervalInMinutes)) {
            return; // do nothing unless at and interval has changed
        }
		_intervalInMinutes = interval;
		_beginAt  = beginAt;
		scheduleJob();
	}
	private Date getBeginAt() {
		Calendar now = Calendar.getInstance();
		Calendar ts  = Calendar.getInstance();

		ts.set(Calendar.DAY_OF_MONTH,	now.get(Calendar.DAY_OF_MONTH));
		ts.set(Calendar.MONTH,			now.get(Calendar.MONTH));
		ts.set(Calendar.YEAR,			now.get(Calendar.YEAR));
		ts.set(Calendar.HOUR,			_beginAt.get(Calendar.HOUR));
		ts.set(Calendar.MINUTE,			_beginAt.get(Calendar.MINUTE));
		ts.set(Calendar.SECOND,			_beginAt.get(Calendar.SECOND));

		// if the starttime has already past then schedule for tommorrow
		if (now.after(ts)) {
            ts.add(Calendar.DAY_OF_MONTH, 1);
        }

		return ts.getTime();
	}
	private class Listener implements JobListener {

		private SearchManager manager;

		public Listener(SearchManager manager) {
			this.manager = manager;
		}

		@Override
		public String getName() {
			return instanceID+"JobListener";
		}

		@Override
		public void jobToBeExecuted(JobExecutionContext context) {
			 if (context.getJobInstance() instanceof LuceneOptimizerJob) {
				 LuceneOptimizerJob job = (LuceneOptimizerJob) context.getJobInstance();
				 job.setIndexTracker(manager.getIndexTracker());
			 }
		}

		@Override
		public void jobExecutionVetoed(JobExecutionContext context) {
			// do nothing
		}

		@Override
		public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
			// do nothing
		}
	}
}
