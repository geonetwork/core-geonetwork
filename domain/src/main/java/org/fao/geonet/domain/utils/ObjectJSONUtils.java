/*
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
 */

package org.fao.geonet.domain.utils;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.Logger;
import org.fao.geonet.utils.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ObjectJSONUtils {

    /**
     * Extract field from JSON string.
     *
     * @param jsonString the json string
     * @param field the field to extract
     * @return the string
     */
    public static String extractFieldFromJSONString(String jsonString, String field) {
        try {
            return ObjectJSONUtils.extractJSONObjectFromJSONString(jsonString).getString(field);
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error extracting field from JSON, error: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Extract field from JSON string from a sub-object.
     *
     * @param jsonString the json string
     * @param subObject the level 1
     * @param field the field to extract
     * @return the string
     */
    public static String extractFieldFromJSONString(String jsonString, String subObject, String field) {
        String[] levels = { subObject };
        return ObjectJSONUtils.extractFieldFromJSONString(jsonString, levels, field);
    }

    /**
     * Extract field from JSON string contained into a path of objects.
     *
     * @param jsonString the json string
     * @param path the levels
     * @param field the field to extract
     * @return the string
     */
    public static String extractFieldFromJSONString(String jsonString, String[] path, String field) {
        try {
            JSONObject current = ObjectJSONUtils.extractJSONObjectFromJSONString(jsonString);
            for (String level : path) {
                current = current.getJSONObject(level);
            }
            return current.getString(field);
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error extracting field from JSON, error: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Extract JSON array from JSON string contained into a path of objects.
     *
     * @param jsonString the json string
     * @param path the levels
     * @return the JSON array
     */
    public static JSONArray extractJSONArrayFromJSONString(String jsonString, String[] path) {
        try {
            JSONObject current = ObjectJSONUtils.extractJSONObjectFromJSONString(jsonString);
            for (String level : path) {
                if (current.get(level) instanceof JSONObject) {
                    current = current.getJSONObject(level);
                } else if (current.get(level) instanceof JSONArray) {
                    return current.getJSONArray(level);
                }
            }
            return null;
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error extracting array from JSON, error: " + e.getMessage(), e);

            return null;
        }
    }

    /**
     * Extract JSON object from JSON string.
     *
     * @param jsonString the json string
     * @return the JSON object
     * @throws JsonProcessingException the json processing exception
     */
    public static JSONObject extractJSONObjectFromJSONString(String jsonString) throws JsonProcessingException {
        return JSONObject.fromObject(jsonString);
    }

    /**
     * Extract list of field from JSON string contained into a sub-object.
     *
     * @param jsonString the json string
     * @param subObject the level 1
     * @param field the field to extract
     * @return the list
     */
    public static List<String> extractListOfFieldFromJSONString(String jsonString, String subObject, String field) {
        String[] levels = { subObject };
        return ObjectJSONUtils.extractListOfFieldFromJSONString(jsonString, levels, field);
    }

    /**
     * Extract list of field from JSON string contained into a path of objects.
     *
     * @param jsonString the json string
     * @param path the levels
     * @param field the field to extract
     * @return the list
     */
    public static List<String> extractListOfFieldFromJSONString(String jsonString, String[] path, String field) {
        try {
            JSONArray array = ObjectJSONUtils.extractJSONArrayFromJSONString(jsonString, path);
            List<String> result = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                result.add(o.getString(field));
            }
            return result;
        } catch (Exception e) {
            Log.error("geonetwork.domain", "Error extracting list of field from JSON, error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts an object in JSONObject.
     *
     * @param object the Object
     * @param objectName the object name
     * @return the JSONObject
     * @throws JsonProcessingException the json processing exception
     */
    public static JSONObject convertObjectInJsonObject(Object object, String objectName) throws JsonProcessingException {

        // The intermediate conversion to String is useful to spot conversion issues
        // not raised with the direct usage of JSONSerializer on the object
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(object);

        JSON json = JSONSerializer.toJSON(jsonString);

        JSONObject container = new JSONObject();
        container.put(objectName, json);

        return container;
    }
}
