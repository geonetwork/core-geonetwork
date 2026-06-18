/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.geonetwork.map.wms;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.geotools.factory.CommonFactoryFinder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SLDUtilTest {

    @Test
    public void testEncodeFilter() throws IOException {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Filter filter = ff.equals(ff.property("name"), ff.literal("test"));
        String encoded = SLDUtil.encodeFilter(filter);
        assertNotNull(encoded);
        assertTrue(encoded.contains("PropertyIsEqualTo"));
        assertTrue(encoded.contains("PropertyName"));
        assertTrue(encoded.contains("name"));
        assertTrue(encoded.contains("Literal"));
        assertTrue(encoded.contains("test"));
    }

    @Test
    public void testGenerateCustomFilterPropertyIsEqualTo() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "name");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "PropertyIsEqualTo");
        JSONArray params = new JSONArray();
        params.put("testValue");
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("name") && filterStr.contains("testValue"));
    }

    @Test
    public void testGenerateCustomFilterPropertyIsBetween() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "age");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "PropertyIsBetween");
        JSONArray params = new JSONArray();
        params.put(10);
        params.put(20);
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("age BETWEEN 10 AND 20"));
    }

    @Test
    public void testGenerateCustomFilterMultipleFiltersAnd() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();

        // Filter 1: name = 'test'
        JSONObject f1 = new JSONObject();
        f1.put("field_name", "name");
        JSONArray fa1 = new JSONArray();
        JSONObject fd1 = new JSONObject();
        fd1.put("filter_type", "PropertyIsEqualTo");
        JSONArray p1 = new JSONArray();
        p1.put("testValue");
        fd1.put("params", p1);
        fa1.put(fd1);
        f1.put("filter", fa1);
        filters.put(f1);

        // Filter 2: age = 30
        JSONObject f2 = new JSONObject();
        f2.put("field_name", "age");
        JSONArray fa2 = new JSONArray();
        JSONObject fd2 = new JSONObject();
        fd2.put("filter_type", "PropertyIsEqualTo");
        JSONArray p2 = new JSONArray();
        p2.put(30);
        fd2.put("params", p2);
        fa2.put(fd2);
        f2.put("filter", fa2);
        filters.put(f2);

        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("name") && filterStr.contains("testValue")
                && filterStr.contains("age") && filterStr.contains("30"));
    }

    @Test
    public void testGenerateCustomFilterMultipleFiltersOr() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();

        // One field with two filters (OR logic)
        JSONObject f1 = new JSONObject();
        f1.put("field_name", "name");
        JSONArray fa1 = new JSONArray();

        JSONObject fd1 = new JSONObject();
        fd1.put("filter_type", "PropertyIsEqualTo");
        JSONArray p1 = new JSONArray();
        p1.put("val1");
        fd1.put("params", p1);
        fa1.put(fd1);

        JSONObject fd2 = new JSONObject();
        fd2.put("filter_type", "PropertyIsEqualTo");
        JSONArray p2 = new JSONArray();
        p2.put("val2");
        fd2.put("params", p2);
        fa1.put(fd2);

        f1.put("filter", fa1);
        filters.put(f1);

        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("name") && filterStr.contains("val1") && filterStr.contains("val2"));
    }

    @Test(expected = JSONException.class)
    public void testGenerateCustomFilterInvalidParamCount() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "name");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "PropertyIsEqualTo");
        JSONArray params = new JSONArray(); // Empty params, should fail
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        SLDUtil.generateCustomFilter(json);
    }

    @Test(expected = JSONException.class)
    public void testGenerateCustomFilterUnknownType() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "name");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "UnknownType");
        JSONArray params = new JSONArray();
        params.put("val");
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        SLDUtil.generateCustomFilter(json);
    }

    @Test
    public void testGenerateCustomFilterPropertyIsBetweenExclusive() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "age");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "PropertyIsBetweenExclusive");
        JSONArray params = new JSONArray();
        params.put(10);
        params.put(20);
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("age") && filterStr.contains("10") && filterStr.contains("20"));
    }

    @Test
    public void testGenerateCustomFilterPropertyIsNull() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter1 = new JSONObject();
        filter1.put("field_name", "name");
        JSONArray filterArray = new JSONArray();
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("filter_type", "PropertyIsNull");
        JSONArray params = new JSONArray(); // No params needed
        filterDetails.put("params", params);
        filterArray.put(filterDetails);
        filter1.put("filter", filterArray);
        filters.put(filter1);
        json.put("filters", filters);

        Filter filter = SLDUtil.generateCustomFilter(json);
        assertNotNull(filter);
        String filterStr = filter.toString();
        assertTrue(filterStr.contains("name") && (filterStr.contains("null") || filterStr.contains("NULL") || filterStr.contains("is null")));
    }
}
