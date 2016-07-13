//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.inspireatom.harvester;

import jeeves.server.context.ServiceContext;
//import jeeves.server.resources.ResourceManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.QuartzSchedulerUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.CronScheduleBuilder;

import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

/**
 * Class to manage about the InspireAtomHarvester schedule.
 *
 * @author Jose García
 */
public class InspireAtomHarvesterScheduler {
    /**
     * Quartz Scheduler identifier
     **/
    private static final String SCHEDULER_ID = "inspireAtomHarvester";
    /**
     * Quartz Scheduler group name
     **/
    private static final String ATOM_HARVESTER_GROUP_NAME = "atomharvester";

    /**
     * Private constructor, to avoid instantiate the class.
     */
    private InspireAtomHarvesterScheduler() {

    }

    /**
     * Method to schedule the atom harvest process.
     *
     * @param cronSchedule Schedule in cron format.
     * @param context      Jeeves context.
     * @param gc           GeoNetwork context.
     * @throws org.quartz.SchedulerException SchedulerException
     */
    public static void schedule(final String cronSchedule, final ServiceContext context, final GeonetContext gc)
        throws SchedulerException {
        //ResourceManager rm = context.getResourceManager();

        // Unschedule previous job
        unSchedule();

        Scheduler scheduler = getScheduler();

        JobDetail job = JobBuilder.newJob(InspireAtomHarvesterJob.class)
            .withIdentity("atomHarvesterId", ATOM_HARVESTER_GROUP_NAME).build();

        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("atomTriggerName", ATOM_HARVESTER_GROUP_NAME)
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cronSchedule))
            // Uncomment for faster testing than cron scheduler
            //SimpleScheduleBuilder.simpleSchedule()
            //    .withIntervalInSeconds(15).repeatForever())
            .build();


        scheduler.start();
        scheduler.scheduleJob(job, trigger);
        if (scheduler.getListenerManager().getJobListener(
            InspireAtomHarvesterJobListener.ATOM_HARVESTER_JOB_CONFIGURATION_LISTENER) == null) {
            scheduler.getListenerManager().addJobListener(
                new InspireAtomHarvesterJobListener(new InspireAtomHarvester(gc)), jobGroupEquals(ATOM_HARVESTER_GROUP_NAME));
        }

        Log.info(Geonet.ATOM, "ATOM feed harvester scheduled");
    }

    /**
     * @throws SchedulerException SchedulerException
     */
    public static void unSchedule() throws SchedulerException {
        Scheduler scheduler = getScheduler();

        scheduler.deleteJob(jobKey("atomHarvesterId", "atomharvester"));

        Log.info(Geonet.ATOM, "ATOM feed harvester unscheduled");
    }

    /**
     * Returns the scheduler for atom harvester.
     *
     * @return Scheduler for atom harvester.
     * @throws SchedulerException SchedulerException
     */
    private static Scheduler getScheduler() throws SchedulerException {
        return QuartzSchedulerUtils.getScheduler(SCHEDULER_ID, true);
    }

    public static void shutdown() {
        try {
            getScheduler().shutdown(true);
        } catch (SchedulerException e) {
            Log.warning(Geonet.ATOM, "Error stopping the scheduler", e);
        }
    }
}
