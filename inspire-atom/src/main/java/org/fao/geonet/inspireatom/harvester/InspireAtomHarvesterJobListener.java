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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Job listener class, used to configure the atom harvester in InspireAtomHarvesterJob.
 *
 * @author Jose García
 */
public class InspireAtomHarvesterJobListener implements JobListener {

    public static final String ATOM_HARVESTER_JOB_CONFIGURATION_LISTENER = "Atom Harvester Job configuration listener";

    /**
     * InspireAtomHarvester instance
     **/
    private InspireAtomHarvester ah;

    public InspireAtomHarvesterJobListener(final InspireAtomHarvester inspireAtomHarvester) {
        this.ah = inspireAtomHarvester;
    }

    @Override
    public String getName() {
        return ATOM_HARVESTER_JOB_CONFIGURATION_LISTENER;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        if (context.getJobInstance() instanceof InspireAtomHarvesterJob) {
            InspireAtomHarvesterJob inspireAtomHarvesterJob = (InspireAtomHarvesterJob) context.getJobInstance();
            inspireAtomHarvesterJob.setHarvester(ah);
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        //nothing
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        // nothing

    }


}
