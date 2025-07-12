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
import org.springframework.util.StringUtils;

import java.sql.SQLException;

public class DatabaseHarvester  extends AbstractHarvester<HarvestResult, DatabaseHarvesterParams> {
    private final static String REGEX_NAME_PREFIX_INVALID_CHARS = "^[^_a-zA-Z]+";
    private final static String REGEX_NAME_INVALID_CHARS = "[^_a-zA-Z0-9]";

    @Override
    protected DatabaseHarvesterParams createParams() {
        return new DatabaseHarvesterParams(dataMan);
    }

    @Override
    protected void storeNodeExtra(DatabaseHarvesterParams params, String path, String siteId, String optionsId) throws SQLException {
        // Remove non-valid characters
        params.setTableName(sanitizeTableName(params.getTableName()));
        params.setMetadataField(sanitizeFieldName(params.getMetadataField()));
        params.setFilterField(sanitizeFieldName(params.getFilterField()));

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
        DatabaseHarvesterAligner h = new DatabaseHarvesterAligner(cancelMonitor, log, context, params, errors);
        result = h.harvest(log);
        log.info("Database harvester end");
    }

    /**
     * Sanitize field names removing invalid characters: The field name should start with a letter or underscore and
     * can contain letters, digits, and underscores.
     *
     * @param fieldName Table field name to sanitize
     * @return Sanitized field name
     */
    private String sanitizeFieldName(String fieldName) {
        return sanitizeValue(fieldName);
    }

    /**
     * Sanitize table name removing invalid characters: The table name should start with a letter or
     * underscore and can contain letters, digits, underscores, and a dot for a qualified table name (schema.table).
     *
     * @param tableName Table name to sanitize, which can be a qualified name (schema.table)
     * @return Sanitized table name
     */
    private String sanitizeTableName(String tableName) {
        if (StringUtils.hasLength(tableName)) {
            String[] parts = tableName.split("\\.", -1);
            if (parts.length == 2) {
                String schema = sanitizeValue(parts[0]);
                String table = sanitizeValue(parts[1]);
                return schema + "." + table;
            } else {
                return sanitizeValue(tableName);
            }
        } else {
            return "";
        }
    }

    /**
     * Sanitize a value for a table nable or field name by removing invalid characters.
     *
     * @param value The value to sanitize
     * @return Sanitized value
     */
    private String sanitizeValue(String value) {
        if (StringUtils.hasLength(value)) {
            return value.replaceAll(REGEX_NAME_PREFIX_INVALID_CHARS, "").replaceAll(REGEX_NAME_INVALID_CHARS, "");
        } else {
            return "";
        }
    }
}
