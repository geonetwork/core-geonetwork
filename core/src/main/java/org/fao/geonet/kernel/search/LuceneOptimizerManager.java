/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.QuartzSchedulerUtils;
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
    private final String instanceID = SCHEDULER_ID + "-" + UUID.randomUUID().toString();
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
        _beginAt = settingsInfo.getLuceneIndexOptimizerSchedulerAt();
        _intervalInMinutes = settingsInfo.getLuceneIndexOptimizerSchedulerInterval();
        if (settingsInfo.getLuceneIndexOptimizerSchedulerEnabled()) {
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

    public void shutdown() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            Log.warning(Geonet.INDEX_ENGINE, "Error stopping the scheduler", e);
        }
    }

    public void reschedule(Calendar beginAt, int interval) throws SchedulerException {
        if (_dateFormat.format(beginAt.getTime()).equals(_dateFormat.format(_beginAt.getTime())) &&
            (interval == _intervalInMinutes)) {
            return; // do nothing unless at and interval has changed
        }
        _intervalInMinutes = interval;
        _beginAt = beginAt;
        scheduleJob();
    }

    private Date getBeginAt() {
        Calendar now = Calendar.getInstance();
        Calendar ts = Calendar.getInstance();

        ts.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
        ts.set(Calendar.MONTH, now.get(Calendar.MONTH));
        ts.set(Calendar.YEAR, now.get(Calendar.YEAR));
        ts.set(Calendar.HOUR, _beginAt.get(Calendar.HOUR));
        ts.set(Calendar.MINUTE, _beginAt.get(Calendar.MINUTE));
        ts.set(Calendar.SECOND, _beginAt.get(Calendar.SECOND));

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
            return instanceID + "JobListener";
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
