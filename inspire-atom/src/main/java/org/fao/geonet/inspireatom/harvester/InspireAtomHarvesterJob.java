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


import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A Quartz job that obtains the Harvester from the JobExecutionContext and executes it.
 *
 * @author Jose García
 */
@DisallowConcurrentExecution
public class InspireAtomHarvesterJob implements Job {

    public static final String ID_FIELD = "atomHarvesterId";
    String harvesterId;
    InspireAtomHarvester inspireAtomHarvester;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Log.info(Geonet.ATOM, "ATOM feed harvester start execution");
            inspireAtomHarvester.harvest();
            Log.info(Geonet.ATOM, "ATOM feed harvester end execution");
        } catch (Throwable t) {
            Log.error(Geonet.ATOM, t.getMessage());
            throw new JobExecutionException(t, false);
        }
    }

    public String getAtomHarvesterId() {
        return harvesterId;
    }

    public void setAtomHarvesterId(String harvesterId) {
        this.harvesterId = harvesterId;
    }

    public void setHarvester(InspireAtomHarvester inspireAtomHarvester) {
        this.inspireAtomHarvester = inspireAtomHarvester;
    }

}