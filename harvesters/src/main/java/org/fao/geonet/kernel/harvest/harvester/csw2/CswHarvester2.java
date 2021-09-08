//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

import org.fao.geonet.Logger;
import org.fao.geonet.client.RemoteHarvesterApiClient;
import org.fao.geonet.client.RemoteHarvesterStatus;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.List;

/**
 * Harvest metadata from other catalogues using the CSW protocol
 */
public class CswHarvester2 extends AbstractHarvester<HarvestResult, CswParams2> {

    @Override
    protected CswParams2 createParams() {
        return new CswParams2(dataMan);
    }

    /**
     * Stores in the harvester settings table some values not managed by {@link AbstractHarvester}
     * @param params the harvester parameters.
     * @param path
     * @param siteId
     * @param optionsId
     * @throws SQLException
     */
    protected void storeNodeExtra(CswParams2 params, String path, String siteId, String optionsId) throws SQLException {

        harvesterSettingsManager.add("id:" + siteId, "capabUrl", params.capabUrl);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "outputSchema", params.outputSchema);
        harvesterSettingsManager.add("id:" + optionsId, "remoteHarvesterNestedServices", params.remoteHarvesterNestedServices);

        //--- store dynamic filter nodes
        String filtersID = harvesterSettingsManager.add(path, "filters", "");

        if (params.eltFilters != null) {
            int i = 1;
            for (Element element : params.eltFilters) {
                String fID = harvesterSettingsManager.add("id:" + filtersID, "filter", "");

                for (Element value : (List<Element>) element.getChildren()) {
                    harvesterSettingsManager.add("id:" + fID, value.getName(), value.getText());
                }

                harvesterSettingsManager.add("id:" + fID, "position", i++);
            }
        }

        if (params.bboxFilter != null) {
            String bboxFilterID = harvesterSettingsManager.add(path, "bboxFilter", "");
            for (Element value : (List<Element>) params.bboxFilter.getChildren()) {
                harvesterSettingsManager.add("id:" + bboxFilterID, value.getName(), value.getText());
            }

        }
    }

    /**
     * @param log
     * @throws Exception
     */
    public void doHarvest(Logger log) throws Exception {
        RemoteHarvester h = new RemoteHarvester(cancelMonitor, log, context, params);
        String processId = "";

        try {
            result = h.harvest(log);

            processId = ((CswRemoteHarvestResult) result).processId;
        } catch (Exception ex) {
            log.error(ex);
            running = false;

            throw ex;
        }

        final String harvesterProcessId = processId;

        new Thread() {
            @Override
            public void run() {
                super.run();

                String url = settingManager.getValue(RemoteHarvesterApiClient.SETTING_REMOTE_HARVESTER_API);
                RemoteHarvesterApiClient remoteHarvesterApiClient = new RemoteHarvesterApiClient(url);
                boolean check = true;
                while (check) {

                    try {
                        RemoteHarvesterStatus harvesterStatus = remoteHarvesterApiClient.retrieveProgress(harvesterProcessId);

                        if (!harvesterStatus.getState().equals("RECORDS_RECEIVED") &&
                            !harvesterStatus.getState().equals("ERROR")) {
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                log.error(e);
                            }
                        } else {
                            CswHarvester2.this.stop(Common.Status.ACTIVE);
                            check = false;
                        }
                    } catch (Exception ex) {
                        // TODO: Handle
                        ex.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public Element getResult() {
        Element resultEl = super.getResult();
        if (result != null) {
            resultEl.addContent(new Element("processID").setText(((CswRemoteHarvestResult) result).processId));
        }

        return resultEl;
    }
}
