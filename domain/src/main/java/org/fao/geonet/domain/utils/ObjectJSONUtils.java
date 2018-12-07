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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ObjectJSONUtils {

    public static String returnField(String jsonString, String field) {
        try {
            return ObjectJSONUtils.returnJsonObjectFromString(jsonString).getString(field);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String returnField(String jsonString, String level1, String field) {
        String[] levels = { level1 };
        return ObjectJSONUtils.returnField(jsonString, levels, field);
    }

    public static String returnField(String jsonString, String[] levels, String field) {
        try {
            JSONObject current = ObjectJSONUtils.returnJsonObjectFromString(jsonString);
            for (String level : levels) {
                current = current.getJSONObject(level);
            }
            return current.getString(field);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static JSONArray returnJSONArrayFromString(String jsonString, String[] levels) {
        try {
            JSONObject current = ObjectJSONUtils.returnJsonObjectFromString(jsonString);
            for (String level : levels) {
                if (current.get(level) instanceof JSONObject) {
                    current = current.getJSONObject(level);
                } else if (current.get(level) instanceof JSONArray) {
                    return current.getJSONArray(level);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject returnJsonObjectFromString(String jsonString) throws JsonProcessingException {
        return JSONObject.fromObject(jsonString);
    }

    public static List<String> returnListOfFieldsFromArrayofObjects(String jsonString, String level1, String field) {
        String[] levels = { level1 };
        return ObjectJSONUtils.returnListOfFieldsFromArrayofObjects(jsonString, levels, field);
    }

    public static List<String> returnListOfFieldsFromArrayofObjects(String jsonString, String[] levels, String field) {
        try {
            JSONArray array = ObjectJSONUtils.returnJSONArrayFromString(jsonString, levels);
            List<String> result = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                JSONObject o = array.getJSONObject(i);
                result.add(o.getString(field));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject wrapObjectWithJsonObject(Object o, String field) throws JsonProcessingException {

        // The conversion to String is useful to spot conversion issues
        // not raised with the direct usage of JSONSerializer on the object
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(o);

        JSON json = JSONSerializer.toJSON(jsonString);

        JSONObject container = new JSONObject();
        container.put(field, json);

        return container;
    }
}
