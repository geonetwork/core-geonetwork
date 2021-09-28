//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.csw2;


import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.client.RemoteHarvesterApiClient;
import org.fao.geonet.client.model.RemoteHarvesterConfiguration;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.setting.SettingManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class RemoteHarvester implements IHarvester<CswRemoteHarvestResult> {
    private final AtomicBoolean cancelMonitor;

    private Logger log;
    private CswParams2 params;
    private ServiceContext context;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();


    public RemoteHarvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, CswParams2 params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
    }

    public CswRemoteHarvestResult harvest(Logger log) throws Exception {
        this.log = log;

        if (cancelMonitor.get()) {
            return new CswRemoteHarvestResult();
        }

        boolean error = false;
        CswRemoteHarvestResult result = new CswRemoteHarvestResult();

        SettingManager sm = context.getBean(SettingManager.class);
        String url = sm.getValue(RemoteHarvesterApiClient.SETTING_REMOTE_HARVESTER_API);
        if (StringUtils.isEmpty(url)) {
            throw new Exception("Remote harvester API endpoint is not configured. Configure it in the Settings page.");
        }

        RemoteHarvesterConfiguration remoteHarvesterConfiguration = new RemoteHarvesterConfiguration();
        remoteHarvesterConfiguration.setUrl(params.capabUrl);
        remoteHarvesterConfiguration.setLongTermTag(params.getName());
        remoteHarvesterConfiguration.setNumberOfRecordsPerRequest(params.numberOfRecordsPerRequest);
        remoteHarvesterConfiguration.setLookForNestedDiscoveryService(params.remoteHarvesterNestedServices);
        remoteHarvesterConfiguration.setErrorConfigDuplicatedUuids(params.errorConfigDuplicatedUuids);
        remoteHarvesterConfiguration.setErrorConfigFewerRecordsThanRequested(params.errorConfigFewerRecordsThanRequested);
        remoteHarvesterConfiguration.setErrorConfigNextRecordsBadValue(params.errorConfigNextRecordsBadValue);
        remoteHarvesterConfiguration.setErrorConfigNextRecordsNotZero(params.errorConfigNextRecordsNotZero);
        remoteHarvesterConfiguration.setErrorConfigTotalRecordsChanged(params.errorConfigTotalRecordsChanged);
        remoteHarvesterConfiguration.setErrorConfigMaxPercentTotalRecordsChangedAllowed(params.errorConfigMaxPercentTotalRecordsChangedAllowed);
        remoteHarvesterConfiguration.setGetRecordQueueHint(params.processQueueType);

        RemoteHarvesterApiClient remoteHarvesterApiClient = new RemoteHarvesterApiClient(url);
        result.processId = remoteHarvesterApiClient.startHarvest(remoteHarvesterConfiguration);

        //remoteHarvesterApiClient.retrieveProgress(result.processId);

        return result;
    }




    public List<HarvestError> getErrors() {
        return errors;
    }




}
