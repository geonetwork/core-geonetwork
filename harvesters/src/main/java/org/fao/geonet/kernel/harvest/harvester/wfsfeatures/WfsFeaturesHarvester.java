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

package org.fao.geonet.kernel.harvest.harvester.wfsfeatures;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;

//=============================================================================

public class WfsFeaturesHarvester extends AbstractHarvester<HarvestResult, WfsFeaturesParams> {
    //---------------------------------------------------------------------------
    //---
    //--- Update
    //---
    //---------------------------------------------------------------------------

    @Override
    protected WfsFeaturesParams createParams() {
        return new WfsFeaturesParams(dataMan);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Harvest
    //---
    //---------------------------------------------------------------------------

    protected void storeNodeExtra(WfsFeaturesParams params, String path,
                                  String siteId, String optionsId) {
        harvesterSettingsManager.add("id:" + siteId, "url", params.url);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + optionsId, "lang", params.lang);
        harvesterSettingsManager.add("id:" + optionsId, "query", params.query);
        harvesterSettingsManager.add("id:" + optionsId, "outputSchema", params.outputSchema);
        harvesterSettingsManager.add("id:" + optionsId, "stylesheet", params.stylesheet);
        harvesterSettingsManager.add("id:" + optionsId, "streamFeatures", params.streamFeatures);
        harvesterSettingsManager.add("id:" + optionsId, "createSubtemplates", params.createSubtemplates);
        harvesterSettingsManager.add("id:" + optionsId, "templateId", params.templateId);
        harvesterSettingsManager.add("id:" + optionsId, "recordsCategory", params.recordsCategory);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public void doHarvest(Logger log) throws Exception {
        Harvester h = new Harvester(cancelMonitor, log, context, params);
        result = h.harvest(log);
    }
}
