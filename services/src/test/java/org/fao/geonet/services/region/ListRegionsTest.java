package org.fao.geonet.services.region;

import org.fao.geonet.services.AbstractServiceIntegrationTest;
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

public class ListRegionsTest extends AbstractServiceIntegrationTest {

    public static final int expectedRegions = 287;
    @Autowired
    private List service;

    @Test
    public void testExecAll() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, null, -1, request);
        HttpMessageConverter<Object> converter = new Jaxb2RootElementHttpMessageConverter();
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        JSONObject json = new JSONObject(Xml.getJSON(outputMessage.getBodyAsString()));
        assertRegions(json);

        converter = new MappingJackson2HttpMessageConverter();
        outputMessage = new MockHttpOutputMessage();
        converter.write(regions, MediaType.APPLICATION_JSON, outputMessage);
        json = new JSONObject(outputMessage.getBodyAsString());
        assertRegions(json);
    }

    @Test
    public void testExecCategorySearch() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, "http://geonetwork-opensource.org/regions#country", -1, request);
//        final JSONObject json = new JSONObject(Xml.getJSON(request));
////        System.out.println(json.toString(2));
//
////        assertEquals("287", json.getString("@count"));
////        assertEquals(287, json.getJSONArray("region").length());
//
//
//        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");
//
//        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
//        assertEquals("false", region.get("@hasGeom"));
//        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
//        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
//        assertEquals("-2.48778", region.getString("east"));
//        assertEquals("10.73526", region.getString("north"));
//        assertEquals("4.34472", region.getString("south"));
//        assertEquals("-8.60638", region.getString("west"));
    }

    @Test
    public void testExecLabelSearch() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", "Ivory Co", null, -1, request);
//        final JSONObject json = new JSONObject(Xml.getJSON(request));
//        System.out.println(json.toString(2));
//
//        assertEquals("1", json.getString("@count"));
//        assertEquals(1, json.getJSONArray("region").length());
//
//
//        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");
//
//        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
//        assertEquals("false", region.get("@hasGeom"));
//        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
//        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
//        assertEquals("-2.48778", region.getString("east"));
//        assertEquals("10.73526", region.getString("north"));
//        assertEquals("4.34472", region.getString("south"));
//        assertEquals("-8.60638", region.getString("west"));
    }

    @Test
    public void testExecMaxResults() throws Exception {
        NativeWebRequest request = new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse());
        ListRegionsResponse regions = service.exec("eng", null, null, 200, request);
//        final JSONObject json = new JSONObject(Xml.getJSON(request));
////        System.out.println(json.toString(2));
//
//        assertEquals("200", json.getString("@count"));
//        assertEquals(200, json.getJSONArray("region").length());
//
//
//        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");
//
//        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
//        assertEquals("false", region.get("@hasGeom"));
//        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
//        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
//        assertEquals("-2.48778", region.getString("east"));
//        assertEquals("10.73526", region.getString("north"));
//        assertEquals("4.34472", region.getString("south"));
//        assertEquals("-8.60638", region.getString("west"));
    }

    private void assertRegions(JSONObject json) throws JSONException {
//        System.out.println(json.toString(2));
        assertEquals("" + expectedRegions, json.getString("@count"));
        assertEquals(expectedRegions, json.getJSONArray("region").length());

        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");
        assertEquals(region.toString(2), "http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
        assertEquals(region.toString(2), "false", region.get("@hasGeom"));
        assertEquals(region.toString(2), "Country", region.getJSONObject("categoryLabel").get("eng"));
        assertEquals(region.toString(2), "Ivory Coast", region.getJSONObject("label").get("eng"));
        assertEquals(region.toString(2), "-2.48778", region.getString("east"));
        assertEquals(region.toString(2), "10.73526", region.getString("north"));
        assertEquals(region.toString(2), "4.34472", region.getString("south"));
        assertEquals(region.toString(2), "-8.60638", region.getString("west"));
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