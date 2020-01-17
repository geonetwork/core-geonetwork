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

package org.fao.geonet.kernel.harvest.harvester.csw;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.jdom.Element;

import java.sql.SQLException;

/**
 * Harvest metadata from other catalogues using the CSW protocol
 */
public class CswHarvester extends AbstractHarvester<HarvestResult, CswParams> {

    @Override
    protected CswParams createParams() {
        return new CswParams(dataMan);
    }

    /**
     * Stores in the harvester settings table some values not managed by {@link AbstractHarvester}
     * @param params the harvester parameters.
     * @param path
     * @param siteId
     * @param optionsId
     * @throws java.sql.SQLException
     */
    protected void storeNodeExtra(CswParams params, String path, String siteId, String optionsId) throws SQLException {

        harvesterSettingsManager.add("id:" + siteId, "capabUrl", params.capabUrl);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "rejectDuplicateResource", params.rejectDuplicateResource);
        harvesterSettingsManager.add("id:" + siteId, "queryScope", params.queryScope);
        harvesterSettingsManager.add("id:" + siteId, "hopCount", params.hopCount);
        harvesterSettingsManager.add("id:" + siteId, "xpathFilter", params.xpathFilter);
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.xslfilter);
        harvesterSettingsManager.add("id:" + siteId, "outputSchema", params.outputSchema);

        //--- store dynamic search nodes
        String searchID = harvesterSettingsManager.add(path, "search", "");

        if (params.eltSearches != null) {
            for (Element element : params.eltSearches) {
                if (!element.getName().startsWith("parser")) {
                    Element value = element.getChild("value");
                    if (value != null) {
                        harvesterSettingsManager.add("id:" + searchID, element.getName(), value.getText());
                    } else {
                        harvesterSettingsManager.add("id:" + searchID, element.getName(), element.getText());
                    }
                }
            }
        }
    }

    /**
     * @param log
     * @throws Exception
     */
    public void doHarvest(Logger log) throws Exception {
        Harvester h = new Harvester(cancelMonitor, log, context, params);
        result = h.harvest(log);
    }
}
