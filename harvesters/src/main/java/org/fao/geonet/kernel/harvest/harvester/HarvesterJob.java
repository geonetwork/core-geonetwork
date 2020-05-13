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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.utils.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A Quartz job that obtains the Harvester from the JobExecutionContext and executes it.
 *
 * In Quartz a job is stateless so they can be scaled.  This is not useful for Geonetwork because
 * the harvester needs certain state like ServiceContext.  So instead a job is submitted to the
 * Scheduler and at the same time a lister is added that listens for that Job. to be executed.  When
 * the Job is to be executed the listener puts the Harvester on the JobExecutionContext.  This Job
 * obtains the harvester from the context and executes it.
 *
 * @author jeichar
 */
@DisallowConcurrentExecution
public class HarvesterJob implements Job, InterruptableJob {

    public static final String ID_FIELD = "harvesterId";
    String harvesterId;
    AbstractHarvester<?, ?> harvester;
    private Thread _this = null;
    protected Logger log = Log.createLogger(Geonet.HARVESTER);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (harvester.isHarvesterTypeDisabled()) {
            log.info("Cancelling harvester " + harvesterId + " execution because harvester type "
                + harvester.getType() + " is disabled in the system settings");
            context.setResult(OperResult.OK);
            return;
        }
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

    public void setHarvesterId(String harvesterId) {
        this.harvesterId = harvesterId;
    }

    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        ApplicationContextHolder.set(applicationContext);
    }

    public void setHarvester(AbstractHarvester<?, ?> harvester) {
        this.harvester = harvester;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        harvester.cancelMonitor.set(true);

        // Following the suggestion of InterruptableJob
        // Sometimes the harvester is frozen
        // give some time, but if it does not finish properly...
        // just kill it!!
        new Thread() {
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
                if (_this.isAlive()) {
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
