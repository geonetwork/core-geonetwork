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

package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;

import java.sql.SQLException;

/**
 * Harvest metadata from a JSON source.
 */
public class SimpleUrlHarvester extends AbstractHarvester<HarvestResult, SimpleUrlParams> {

    @Override
    protected SimpleUrlParams createParams() {
        return new SimpleUrlParams(dataMan);
    }

    /**
     * Stores in the harvester settings table some values not managed by {@link AbstractHarvester}
     *
     * @param params         the harvester parameters.
     * @param path
     * @param siteId
     * @param optionsId
     * @throws SQLException
     */
    protected void storeNodeExtra(SimpleUrlParams params, String path, String siteId, String optionsId) throws SQLException {

        harvesterSettingsManager.add("id:" + siteId, "url", params.url);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "loopElement", params.loopElement);
        harvesterSettingsManager.add("id:" + siteId, "numberOfRecordPath", params.numberOfRecordPath);
        harvesterSettingsManager.add("id:" + siteId, "recordIdPath", params.recordIdPath);
        harvesterSettingsManager.add("id:" + siteId, "pageFromParam", params.pageFromParam);
        harvesterSettingsManager.add("id:" + siteId, "pageSizeParam", params.pageSizeParam);
        harvesterSettingsManager.add("id:" + siteId, "toISOConversion", params.toISOConversion);
    }

    public void doHarvest(Logger log) throws Exception {
        Harvester h = new Harvester(cancelMonitor, log, context, params, errors);
        result = h.harvest(log);
    }
}
