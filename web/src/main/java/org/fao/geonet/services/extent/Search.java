//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.extent;

import static org.fao.geonet.services.extent.ExtentHelper.CLEAR_SELECTION;
import static org.fao.geonet.services.extent.ExtentHelper.getSelection;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import jeeves.server.context.ServiceContext;

import jeeves.utils.Util;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Searches for matching extents. It is either a Like search or a XML filter
 * 
 * @author jeichar
 */
public class Search extends List
{

    private enum SearchMethod
    {
        LOOSE, STRICT, STARTS_WITH, ENDS_WITH;

        public static SearchMethod lookup(String method)
        {
            for (final SearchMethod sm : values()) {
                if (sm.name().equalsIgnoreCase(method)) {
                    return sm;
                }
            }
            return STRICT;
        }
    }

    private final Parser         filter11Parser = new Parser(new org.geotools.filter.v1_1.OGCConfiguration());
    private final Parser         filter10Parser = new Parser(new org.geotools.filter.v1_0.OGCConfiguration());
    private final FilterFactory2 filterFactory  = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
    {
        filter10Parser.setFailOnValidationError(false);
        filter10Parser.setStrict(false);
        filter10Parser.setValidating(false);
        filter11Parser.setFailOnValidationError(false);
        filter11Parser.setStrict(false);
        filter11Parser.setValidating(false);
    }

    @Override
    protected String validateParams(Element params)
    {
        final String xml = Util.getParamText(params, "xml");
        if (xml != null) {
            final String version = Util.getParamText(params, "version");
            if (version == null) {
                return "The version parameter must be specified if an XML filter is used";
            }
        } else {
            final String pattern = Util.getParamText(params, "pattern");
            final String property = Util.getParamText(params, "property");
            if (pattern == null) {
                return "Either an xml filter or pattern and property parameters must be available";
            }

            if (property == null) {
                return "A property parameter is required";
            }
            if (!(property.equalsIgnoreCase("desc") || property.equalsIgnoreCase("id"))) {
                return "property must be either 'desc' or 'id'";
            }
        }
        return null;
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception
    {

        final String clearSelection = Util.getParamText(params, CLEAR_SELECTION);
        if (clearSelection != null && Boolean.parseBoolean(clearSelection)) {
            getSelection(context).ids.clear();
        }
        return super.exec(params, context);
    }

    @Override
    protected Query createQuery(Element params, FeatureType featureType, String[] properties, int maxFeatures)
            throws Exception
    {
        final String xml = Util.getParamText(params, "xml");
        DefaultQuery query;
        if (xml != null) {
            final String version = Util.getParamText(params, "version");
            query = xmlFilterQuery(xml, version, featureType, properties);
        } else {
            query = likeQuery(params, featureType, properties);
            String wkt = Util.getParamText(params, "geom");
            if (wkt != null && query.getFilter() != Filter.EXCLUDE) {
                Filter geomFilter = geomFilter(wkt, featureType);
                Filter filter = query.getFilter();
                if (filter == Filter.INCLUDE) {
                    query.setFilter(geomFilter);
                } else {
                    And anded = filterFactory.and(filter, geomFilter);
                    query.setFilter(anded);
                }
            }
        }
        query.setMaxFeatures(maxFeatures);

        return query;

    }

    private Filter geomFilter(String wkt, FeatureType featureType) throws Exception
    {
        Geometry geometry = new WKTReader().read(wkt);
        PropertyName geomAtt = filterFactory.property(featureType.getFeatureSource().getSchema()
                .getGeometryDescriptor().getLocalName());
        return filterFactory.within(geomAtt, filterFactory.literal(geometry));
    }

    private DefaultQuery xmlFilterQuery(String xml, String version, FeatureType featureType, String[] properties)
            throws Exception
    {
        Filter filter;
        if (version.startsWith("1.0")) {
            filter = (Filter) filter10Parser.parse(new StringReader(xml));
        } else {
            filter = (Filter) filter11Parser.parse(new StringReader(xml));
        }
        return featureType.createQuery(filter, properties);
    }

    private DefaultQuery likeQuery(Element params, FeatureType featureType, String[] properties) throws IOException
    {
        String pattern = Util.getParamText(params, "pattern") + "";
        final String method = Util.getParamText(params, "method");
        String propertyText = Util.getParamText(params, "property");

        if (propertyText.equalsIgnoreCase("id")) {
            propertyText = featureType.idColumn;
        } else if (featureType.searchColumn != null) {
            propertyText = featureType.searchColumn;
            String[] newProps = new String[properties.length + 1];
            System.arraycopy(properties, 0, newProps, 0, properties.length);
            newProps[newProps.length - 1] = featureType.searchColumn;
            properties = newProps;
        } else {
            propertyText = featureType.descColumn;
        }

        java.util.List<Filter> filters = new ArrayList<Filter>();

        for (String s : LangUtils.analyzeForSearch(new StringReader(pattern))) {
            String token = updatePattern(method, s);

            final Expression property = filterFactory.property(propertyText);
            final PropertyIsLike likeFilter = filterFactory.like(property, token);
            filters.add(likeFilter);
        }

        if (filters.isEmpty()) {
            return featureType.createQuery(properties);
        } else if (filters.size() == 1) {
            return featureType.createQuery(filters.get(0), properties);
        } else {
            return featureType.createQuery(filterFactory.and(filters), properties);
        }
    }

    private String updatePattern(String method, String pattern)
    {
        switch (SearchMethod.lookup(method))
        {
        case LOOSE:
            return "*" + pattern + "*";

        case STARTS_WITH:
            return pattern + "*";

        case ENDS_WITH:
            return "*" + pattern;

        default:
            return "*" + pattern + "*";
        }

    }
}
