//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.webdav;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;

import java.sql.SQLException;

public class WebDavHarvester extends AbstractHarvester<HarvestResult, WebDavParams> {
    @Override
    protected WebDavParams createParams() {
        return new WebDavParams(dataMan);
    }

    //---------------------------------------------------------------------------
    @Override
    protected void storeNodeExtra(WebDavParams params, String path, String siteId, String optionsId) throws SQLException {
        harvesterSettingsManager.add("id:" + siteId, "url", params.url);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + optionsId, "validate", params.getValidate());
        harvesterSettingsManager.add("id:" + optionsId, "recurse", params.recurse);
        harvesterSettingsManager.add("id:" + optionsId, "subtype", params.subtype);
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.xslfilter);
    }

    public void doHarvest(Logger log) throws Exception {
        log.info("WebDav doHarvest start");
        try (Harvester h = new Harvester(cancelMonitor, log, context, params)) {
            result = h.harvest(log);
        }
        log.info("WebDav doHarvest end");
    }
}
