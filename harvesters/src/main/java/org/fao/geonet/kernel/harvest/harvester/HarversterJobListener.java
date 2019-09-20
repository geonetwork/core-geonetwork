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
 * This is necessary since Harvester objects could contain state (even if it is bad). So the same
 * Harvester object must be used.
 *
 * @author jeichar
 */
public class HarversterJobListener implements JobListener {

    private static final String HARVESTER_JOB_CONFIGURATION_LISTENER = "Harvester Job configuration listener";
    private static HarversterJobListener INSTANCE = new HarversterJobListener() {
    };
    private List<HarvestManager> listOfManager = new ArrayList<HarvestManager>();

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
            AbstractHarvester<?, ?> harvester = null;
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
