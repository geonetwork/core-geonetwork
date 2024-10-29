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

package org.fao.geonet.kernel.harvest.harvester.oaipmh;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;

import java.sql.SQLException;

//=============================================================================

public class OaiPmhHarvester extends AbstractHarvester<HarvestResult, OaiPmhParams> {
    //---------------------------------------------------------------------------
    //---
    //--- Update
    //---
    //---------------------------------------------------------------------------

    @Override
    protected OaiPmhParams createParams() {
        return new OaiPmhParams(dataMan);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Harvest
    //---
    //---------------------------------------------------------------------------

    protected void storeNodeExtra(OaiPmhParams params, String path,
                                  String siteId, String optionsId) throws SQLException {
        harvesterSettingsManager.add("id:" + siteId, "url", params.url);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.xslfilter);

        harvesterSettingsManager.add("id:" + optionsId, "validate", params.getValidate());

        //--- store search nodes

        for (Search s : params.getSearches()) {
            String searchID = harvesterSettingsManager.add(path, "search", "");

            harvesterSettingsManager.add("id:" + searchID, "from", s.from);
            harvesterSettingsManager.add("id:" + searchID, "until", s.until);
            harvesterSettingsManager.add("id:" + searchID, "set", s.set);
            harvesterSettingsManager.add("id:" + searchID, "prefix", s.prefix);
            harvesterSettingsManager.add("id:" + searchID, "stylesheet", s.stylesheet);
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public void doHarvest(Logger log) throws Exception {
        try (Harvester h = new Harvester(cancelMonitor, log, context, params)) {
            result = h.harvest(log);
        }
    }
}
