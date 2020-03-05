/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.regions;

import org.fao.geonet.api.regions.ListRegionsResponse;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.region.List;
import org.fao.geonet.utils.Xml;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;

public class ListRegionsTest extends AbstractServiceIntegrationTest {

    @Autowired
    private List service;

    @Test
    @Ignore
    public void testExecAll() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, null, -1, request);

        final int expectedRegions = 287;

        assertEquals(expectedRegions, regions.size());

        HttpMessageConverter<Object> converter = new Jaxb2RootElementHttpMessageConverter();
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        JSONObject json = new JSONObject(Xml.getJSON(outputMessage.getBodyAsString()));
        assertRegions(json, expectedRegions, true);

        converter = new MappingJackson2HttpMessageConverter();
        outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        json = new JSONObject(outputMessage.getBodyAsString());
        assertRegions(json, expectedRegions, false);
    }

    @Test
    @Ignore
    public void testExecCategorySearch() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, "http://geonetwork-opensource.org/regions#country", -1, request);

        final int expectedRegions = 278;
        HttpMessageConverter<Object> converter = new Jaxb2RootElementHttpMessageConverter();
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        JSONObject json = new JSONObject(Xml.getJSON(outputMessage.getBodyAsString()));
        assertRegions(json, expectedRegions, true);

        converter = new MappingJackson2HttpMessageConverter();
        outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        json = new JSONObject(outputMessage.getBodyAsString());
        assertRegions(json, expectedRegions, false);
    }

    @Test
    @Ignore
    public void testExecLabelSearch() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", "iv", null, -1, request);

        final int expectedRegions = 3;
        HttpMessageConverter<Object> converter = new Jaxb2RootElementHttpMessageConverter();
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        JSONObject json = new JSONObject(Xml.getJSON(outputMessage.getBodyAsString()));
        assertRegions(json, expectedRegions, true);

        converter = new MappingJackson2HttpMessageConverter();
        outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        json = new JSONObject(outputMessage.getBodyAsString());
        assertRegions(json, expectedRegions, false);
    }

    @Test
    @Ignore
    public void testExecMaxResults() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, null, 200, request);

        final int expectedRegions = 200;
        HttpMessageConverter<Object> converter = new Jaxb2RootElementHttpMessageConverter();
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        JSONObject json = new JSONObject(Xml.getJSON(outputMessage.getBodyAsString()));
        assertRegions(json, expectedRegions, true);

        converter = new MappingJackson2HttpMessageConverter();
        outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        json = new JSONObject(outputMessage.getBodyAsString());
        assertRegions(json, expectedRegions, false);
    }

    private void assertRegions(JSONObject json, int expectedRegions, boolean fromXml) throws JSONException {
//        System.out.println(json.toString(2));
        assertEquals("" + expectedRegions, json.getString("@count"));
        assertEquals(expectedRegions, json.getJSONArray("region").length());

        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");
        final String categoryId = region.getString("@categoryId");
        assertEquals(region.toString(2), "http://geonetwork-opensource.org/regions#country", categoryId);
        assertEquals(region.toString(2), "false", region.get("@hasGeom").toString());
        if (fromXml) {
            JSONObject categoryDef = (JSONObject) findInJsonArray(categoryId, json.get("categories"));
            assertEquals(region.toString(2), "Country", findLabel(categoryDef, "eng"));
            assertEquals(region.toString(2), "Ivory Coast", findLabel(region, "eng"));
        } else {
            final JSONObject categoryDef = json.getJSONObject("categories").getJSONObject(categoryId);
            assertEquals(region.toString(2), "Country", categoryDef.getJSONObject("label").get("eng"));
            assertEquals(region.toString(2), "Ivory Coast", region.getJSONObject("label").get("eng"));
        }
        assertEquals(region.toString(2), "-2.48778", region.getString("east"));
        assertEquals(region.toString(2), "10.73526", region.getString("north"));
        assertEquals(region.toString(2), "4.34472", region.getString("south"));
        assertEquals(region.toString(2), "-8.60638", region.getString("west"));
    }

    private String findLabel(JSONObject categoryDef, String lang) throws JSONException {
        JSONArray labels = categoryDef.getJSONArray("label");
        return (String) findInJsonArray(lang, labels);
    }

    private Object findInJsonArray(String requiredKey, Object obj) throws JSONException {
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            for (int i = 0; i < array.length(); i++) {
                final JSONObject entry = array.getJSONObject(i);
                if (requiredKey.equals(entry.getString("key"))) {
                    return entry.get("value");
                }
            }
        } else if (obj instanceof JSONObject) {
            JSONObject entry = ((JSONObject) obj).getJSONObject("entry");
            if (requiredKey.equals(entry.getString("key"))) {
                return entry.get("value");
            }
        }
        return null;
    }

    private JSONObject findRegion(JSONObject json, String regionId) throws JSONException {
        JSONArray regions = json.getJSONArray("region");

        for (int i = 0; i < regions.length(); i++) {
            final JSONObject region = regions.getJSONObject(i);
            if (region.getString("id").equals(regionId)) {
                return region;
            }
        }
        throw new AssertionError("No region found with id: " + regionId);
    }


}
