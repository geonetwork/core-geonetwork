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

package org.fao.geonet.utils;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.quartz.CronExpression;
import org.quartz.DateBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.StdSchedulerFactory;

public final class QuartzSchedulerUtils {

    private QuartzSchedulerUtils() {
    }

    public static Scheduler getScheduler(String id, boolean startSchedulerWhenNotStarted) throws SchedulerException {

        String schedName = id + ".scheduler";
        Scheduler scheduler = new StdSchedulerFactory().getScheduler(schedName);
        if (scheduler == null) {
            String quartzConfigurationFile = "quartz-" + id + ".properties";
            InputStream in = QuartzSchedulerUtils.class.getClassLoader().getResourceAsStream(quartzConfigurationFile);
            try {
                if (in == null) {
                    scheduler = StdSchedulerFactory.getDefaultScheduler();
                } else {
                    Properties configuration = new Properties();
                    configuration.load(in);
                    configuration.put("org.quartz.scheduler.instanceName", schedName);
                    StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
                    stdSchedulerFactory.initialize(configuration);
                    scheduler = stdSchedulerFactory.getScheduler();
                }
            } catch (IOException e) {
                throw new SchedulerException("Unable to load configuration for scheduler " + id + ".  Configuration file "
                    + quartzConfigurationFile + " exists and was loaded but an error occurred during loading", e);
            } finally {
                if (in != null) {
                    IOUtils.closeQuietly(in);
                }
            }
        }

        if (startSchedulerWhenNotStarted && !scheduler.isStarted())
            scheduler.start();

        return scheduler;

    }

    /**
     * Parse string and create a trigger with provided id and group name.
     *
     * Formats are cron format ({@link CronExpression}, integer representing period in minutes or
     * format like 10 hours, 5 minutes, 30 seconds, which will be parsed to create a period.
     *
     * @param id        id of trigger
     * @param groupName groupName to assign to trigger
     * @param schedule  the schedule string that needs to be parsed.
     * @param maxEvery  the maximum period to permit
     * @return trigger object
     */
    public static Trigger getTrigger(String id, String groupName, String schedule, long maxEvery) {
        TriggerBuilder<Trigger> trigger = newTrigger().withIdentity(id, groupName);
        try {
            trigger.withSchedule(cronSchedule(new CronExpression(schedule)));
        } catch (ParseException e) {
            int periodMillis = 0;
            try {
                int everyMin = Integer.parseInt(schedule);
                periodMillis = everyMin * 1000 * 60;
            } catch (NumberFormatException nfe) {

                int mult = 0;

                StringTokenizer st = new StringTokenizer(schedule, ",");

                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim().toLowerCase();

                    if (token.endsWith(" hour")) {
                        token = token.substring(0, token.length() - 5);
                        mult = 3600;
                    } else if (token.endsWith(" hours")) {
                        token = token.substring(0, token.length() - 6);
                        mult = 3600;
                    } else if (token.endsWith(" min")) {
                        token = token.substring(0, token.length() - 4);
                        mult = 60;
                    } else if (token.endsWith(" sec")) {
                        token = token.substring(0, token.length() - 4);
                        mult = 1;
                    } else
                        throw new IllegalArgumentException("Bad period format :" + schedule);

                    periodMillis += mult * Integer.parseInt(token);
                }
            }
            if (periodMillis < 1 || periodMillis > maxEvery)
                throw new IllegalArgumentException(schedule + " is an illegal value, it must be between 1 and " + maxEvery);

            trigger.withSchedule(simpleSchedule().withIntervalInMilliseconds(periodMillis).repeatForever().withMisfireHandlingInstructionFireNow()).
                startAt(DateBuilder.futureDate(periodMillis, IntervalUnit.MILLISECOND));
        }

        return trigger.build();
    }
}
