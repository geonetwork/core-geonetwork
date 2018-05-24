/*
 * Copyright (C) 2001-2018 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package v342;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.migration.DatabaseMigrationException;
import org.fao.geonet.migration.JsonDatabaseMigration;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds a new field <code>mods.search.advancedSearchTemplate</code> to <code>ui/config</code> setting to customise
 * the advanced search form using a template.
 */
public class AdvancedSearchFormMigration extends JsonDatabaseMigration
    implements DatabaseMigrationTask {

    @Override
    public void update(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM settings WHERE name='ui/config'")) {
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String currentSettingJson = rs.getString("value");
                Map<String, String> fieldsToUpdate = new HashMap<>(1);
                fieldsToUpdate.put("/mods/search/advancedSearchTemplate",
                    "\"../../catalog/views/default/templates/advancedSearchForm/defaultAdvancedSearchForm.html\"");

                String newSettingJson = super.insertOrUpdateField(currentSettingJson, fieldsToUpdate);
                try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE settings SET "
                    + "value=? WHERE name='ui/config'")) {
                    updateStatement.setString(1, newSettingJson);
                    updateStatement.executeUpdate();
                }
            }
        } catch (IOException e) {
            Log.error(Geonet.GEONETWORK + ".databasemigration",
                "Error in AdvancedSearchFormMigration. Cannot complete the "
                    + "migration", e);
            throw new DatabaseMigrationException(e);
        }

    }

    /**
     * Given a JSON string and a map of fields to update/insert and their values return an updated JSON string.
     *
     * @param currentSettingJson the JSON string to modify.
     * @param fieldsToUpdate     a {@link Map} with the fields to update. Keys are in the form of nested fields separated by
     *                           slash ("/") and starting by slash. Values are in form of JSON strings. For example
     *                           <code>"\"my new value\""</code>
     * @return a JSON string with updated/inserted values.
     */
    @VisibleForTesting
    @Override
    protected String insertOrUpdateField(String currentSettingJson, Map<String, String> fieldsToUpdate) throws IOException {
        return super.insertOrUpdateField(currentSettingJson, fieldsToUpdate);

    }

    @VisibleForTesting
    @Override
    protected void createMissingNodes(JsonNode root, List<String> fullPath, JsonNode newJsonTree) {
        super.createMissingNodes(root, fullPath, newJsonTree);
    }


}
