package org.fao.geonet.services.region;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ListRegionsTest extends AbstractServiceIntegrationTest {

    @Test
    public void testExecAll() throws Exception {
        final List service = new List();
        final Element request = service.exec(new Element("request"), createServiceContext());
        final JSONObject json = new JSONObject(Xml.getJSON(request));
//        System.out.println(json.toString(2));

        assertEquals("287", json.getString("@count"));
        assertEquals(287, json.getJSONArray("region").length());

        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");

        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
        assertEquals("false", region.get("@hasGeom"));
        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
        assertEquals("-2.48778", region.getString("east"));
        assertEquals("10.73526", region.getString("north"));
        assertEquals("4.34472", region.getString("south"));
        assertEquals("-8.60638", region.getString("west"));
    }

    @Test
    public void testExecCategorySearch() throws Exception {
        final List service = new List();
        final Element request = service.exec(createParams(Pair.read("categoryId", "http://geonetwork-opensource.org/regions#country")), createServiceContext());
        final JSONObject json = new JSONObject(Xml.getJSON(request));
//        System.out.println(json.toString(2));

//        assertEquals("287", json.getString("@count"));
//        assertEquals(287, json.getJSONArray("region").length());


        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");

        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
        assertEquals("false", region.get("@hasGeom"));
        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
        assertEquals("-2.48778", region.getString("east"));
        assertEquals("10.73526", region.getString("north"));
        assertEquals("4.34472", region.getString("south"));
        assertEquals("-8.60638", region.getString("west"));
    }

    @Test
    public void testExecLabelSearch() throws Exception {
        final List service = new List();
        final Element request = service.exec(createParams(Pair.read("label", "Ivory Co")), createServiceContext());
        final JSONObject json = new JSONObject(Xml.getJSON(request));
        System.out.println(json.toString(2));

        assertEquals("1", json.getString("@count"));
        assertEquals(1, json.getJSONArray("region").length());


        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");

        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
        assertEquals("false", region.get("@hasGeom"));
        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
        assertEquals("-2.48778", region.getString("east"));
        assertEquals("10.73526", region.getString("north"));
        assertEquals("4.34472", region.getString("south"));
        assertEquals("-8.60638", region.getString("west"));
    }

    @Test
    public void testExecMaxResults() throws Exception {
        final List service = new List();
        final Element request = service.exec(createParams(Pair.read("maxRecords", "200")), createServiceContext());
        final JSONObject json = new JSONObject(Xml.getJSON(request));
//        System.out.println(json.toString(2));

        assertEquals("200", json.getString("@count"));
        assertEquals(200, json.getJSONArray("region").length());


        JSONObject region = findRegion(json, "http://geonetwork-opensource.org/regions#107");

        assertEquals("http://geonetwork-opensource.org/regions#country", region.get("@categoryId"));
        assertEquals("false", region.get("@hasGeom"));
        assertEquals("Country", region.getJSONObject("categoryLabel").get("eng"));
        assertEquals("Ivory Coast", region.getJSONObject("label").get("eng"));
        assertEquals("-2.48778", region.getString("east"));
        assertEquals("10.73526", region.getString("north"));
        assertEquals("4.34472", region.getString("south"));
        assertEquals("-8.60638", region.getString("west"));
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