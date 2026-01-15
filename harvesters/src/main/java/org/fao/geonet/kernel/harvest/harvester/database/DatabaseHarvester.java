//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.database;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;

import java.sql.SQLException;

public class DatabaseHarvester  extends AbstractHarvester<HarvestResult, DatabaseHarvesterParams> {
    @Override
    protected DatabaseHarvesterParams createParams() {
        return new DatabaseHarvesterParams(dataMan);
    }

    @Override
    protected void storeNodeExtra(DatabaseHarvesterParams params, String path, String siteId, String optionsId) throws SQLException {
        // Remove non-valid characters
        params.setTableName(DatabaseHarvesterUtil.sanitizeTableName(params.getTableName()));
        params.setMetadataField(DatabaseHarvesterUtil.sanitizeFieldName(params.getMetadataField()));
        params.setFilterField(DatabaseHarvesterUtil.sanitizeFieldName(params.getFilterField()));

        setParams(params);

        harvesterSettingsManager.add("id:" + siteId, "icon", params.getIcon());
        harvesterSettingsManager.add("id:" + siteId, "server", params.getServer());
        harvesterSettingsManager.add("id:" + siteId, "port", params.getPort());
        harvesterSettingsManager.add("id:" + siteId, "username", params.getUsername());
        harvesterSettingsManager.add("id:" + siteId, "password", params.getPassword());
        harvesterSettingsManager.add("id:" + siteId, "database", params.getDatabase());
        harvesterSettingsManager.add("id:" + siteId, "databaseType", params.getDatabaseType());
        harvesterSettingsManager.add("id:" + siteId, "tableName", params.getTableName());
        harvesterSettingsManager.add("id:" + siteId, "metadataField", params.getMetadataField());
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.getXslfilter());

        String filtersID = harvesterSettingsManager.add(path, "filter", "");
        harvesterSettingsManager.add("id:" + filtersID, "field", params.getFilterField());
        harvesterSettingsManager.add("id:" + filtersID, "value", params.getFilterValue());
        harvesterSettingsManager.add("id:" + filtersID, "operator", params.getFilterOperator());
    }

    @Override
    protected void doHarvest(Logger l) throws Exception {
        log.info("Database harvester start");
        try (DatabaseHarvesterAligner h = new DatabaseHarvesterAligner(cancelMonitor, log, context, params, errors)) {
            result = h.harvest(log);
        }
        log.info("Database harvester end");
    }
}
