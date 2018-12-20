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
package org.fao.geonet.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Helper class for handling JSON strings.
 */
public abstract class JsonDatabaseMigration extends DatabaseMigrationTask {

    @Override
    public void update(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM settings WHERE name=?")) {
            statement.setString(1, getSettingName());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String currentSettingJson = rs.getString("value");
                if (StringUtils.isNotEmpty(currentSettingJson)) {
                    Map<String, String> fieldsToUpdate = setUpNewSettingValues();

                    String newSettingJson = insertOrUpdateField(currentSettingJson, fieldsToUpdate);
                    try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE settings SET "
                        + "value=? WHERE name=?")) {
                        updateStatement.setString(1, newSettingJson);
                        updateStatement.setString(2, getSettingName());
                        updateStatement.executeUpdate();
                    }
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
    protected String insertOrUpdateField(String currentSettingJson, Map<String, String> fieldsToUpdate) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(currentSettingJson);
        for (Map.Entry<String, String> fieldToUpdate : fieldsToUpdate.entrySet()) {
            String fullPath = fieldToUpdate.getKey();
            String newJsonString = fieldToUpdate.getValue();
            JsonNode newJsonTree = mapper.readTree(newJsonString);
            JsonNode target = root.at(fullPath);
            List<String> pathParts = new LinkedList<>();
            pathParts.addAll(Arrays.asList(StringUtils.split(fullPath, "/")));
            if (target.isMissingNode()) {
                createMissingNodes(root, pathParts, newJsonTree);
            } else {
                String propertyName = pathParts.remove(pathParts.size() - 1);
                target = root.at("/" + StringUtils.join(pathParts, "/"));
                ((ObjectNode) target).set(propertyName, newJsonTree);
            }
        }
        return mapper.writeValueAsString(root);

    }

    @VisibleForTesting
    protected void createMissingNodes(JsonNode root, List<String> fullPath, JsonNode newJsonTree) {
        JsonNode newRoot = root;
        for (int i = 0; i < fullPath.size() - 1; i++) {
            String pathPart = fullPath.get(i);
            newRoot = newRoot.with(pathPart);
        }
        ((ObjectNode) newRoot).set(fullPath.get(fullPath.size() - 1), newJsonTree);
    }


    /**
     *
     * @return a map where the key is the JSON path to the property to add or modify and the values are the new values
     * for the path in key.
     */
    protected abstract Map<String, String> setUpNewSettingValues();

    /**
     *
     * @return the name of the setting to update.
     */
    protected abstract String getSettingName();
}
