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

import org.fao.geonet.constants.Geonet;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class SLDUtil {

    public static final String LOGGER = Geonet.GEONETWORK + ".util.sld";

    /**
     * Encode into a string the given OGC Filter
     *
     * @param filter the OGC filter object
     * @return String the filter object to String
     * @throws IOException
     */
    public static String encodeFilter(Filter filter) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();

        Configuration configuration = new OGCConfiguration();
        Encoder encoder = new Encoder(configuration);
        encoder.encode( filter, org.geotools.filter.v1_1.OGC.Filter, outputStream);

        return outputStream.toString();
    }

    /**
     * Generate a SLD Filter from filters defined in a JSON
     *
     * JSON example :
     * <pre>
     * {
     *     "baseStyle" : "Test:MuiltiRoad",
     *     "filters": [ {"field_name": "longueur",
     *                   "filter": [ { "filter_type": "PropertyIsBetween",
     *                                 "params": [0,500]},
     *                               { "filter_type": "PropertyIsBetween",
     *                                 "params": [500,5000]}]},
     *                  {"field_name": "departement",
     *                   "filter": [ { "filter_type": "PropertyIsEqualTo",
     *                                 "params": ["Ain"]}]},
     *                  {"field_name": "date_renovation",
     *                   "filter": [ { "filter_type": "PropertyIsBetween",
     *                                 "params": ["2015-07-01", "2015-08-31"]},
     *                               { "filter_type": "PropertyIsBetween",
     *                                 "params": ["2014-07-01", "2014-08-31"]}]}
     *                ]
     * }</pre>
     *
     * @param userFilters JSON representation of filters
     * @return Filter instance that represent combination of filters specified in JSON
     * @throws JSONException if some have wrong parameter count or malformed JSON
     */

    public static Filter generateCustomFilter(JSONObject userFilters) throws JSONException {
        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        JSONArray filters = userFilters.getJSONArray("filters");
        List<Filter> res = new LinkedList<>();

        for(int i=0;i<filters.length();i++)
            res.add(SLDUtil.generateFilter(filters.getJSONObject(i)));

        if(res.size() > 1)
            return ff2.and(res);
        else
            return res.get(0);

    }

    private static Filter generateFilter(JSONObject jsonObject) throws JSONException {

        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        String fieldName = jsonObject.getString("field_name");

        List<Filter> res = new LinkedList<>();

        JSONArray filters = jsonObject.getJSONArray("filter");
        for(int i=0;i<filters.length();i++)
            res.add(SLDUtil.generateFilter2(fieldName, filters.getJSONObject(i)));

        if(res.size() > 1)
            return ff2.or(res);
        else
            return  res.get(0);
    }

    private static Filter generateFilter2(String fieldName, JSONObject jsonObject) throws JSONException {

        FilterFactory2 ff2 = CommonFactoryFinder.getFilterFactory2();

        String filterType = jsonObject.getString("filter_type");

        List parameters = new LinkedList();

        JSONArray params = jsonObject.getJSONArray("params");
        for(int i=0;i<params.length();i++)
            parameters.add(params.get(i));

        if(filterType.equals("PropertyIsEqualTo")) {
            if(parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.equals(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsNotEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.notEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLessThan")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.less(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLessThanOrEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.lessOrEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsGreaterThan")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.greater(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsGreaterThanOrEqualTo")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.greaterOrEqual(ff2.property(fieldName), ff2.literal(parameters.get(0)));
        } else if(filterType.equals("PropertyIsLike")) {
            if (parameters.size() != 1) throw new JSONException("Invalid parameter count");
            return ff2.like(ff2.property(fieldName), (String) parameters.get(0));
        } else if(filterType.equals("PropertyIsNull")) {
            if (!parameters.isEmpty()) throw new JSONException("Invalid parameter count");
            return ff2.isNull(ff2.property(fieldName));
        } else if(filterType.equals("PropertyIsBetween")) {
            if (parameters.size() != 2) throw new JSONException("Invalid parameter count");
            return ff2.between(ff2.property(fieldName), ff2.literal(parameters.get(0)), ff2.literal(parameters.get(1)));
        } else if(filterType.equals("PropertyIsBetweenExclusive")) {
            if (parameters.size() != 2) throw new JSONException("Invalid parameter count");
            return ff2.and(
                ff2.greater(ff2.property(fieldName), ff2.literal(parameters.get(0))),
                ff2.less(ff2.property(fieldName), ff2.literal(parameters.get(1)))
            );
        } else {
            // Currently, no implementation of topological or distance operators
            throw new JSONException("No implementation for filter type : " + filterType);
        }

    }

}
