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

import static org.fao.geonet.services.extent.ExtentHelper.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Service for adding new Geometries to a the updateable wfs featuretype
 * 
 * @author jeichar
 * 
 */
public class Add implements Service
{

    enum Format
    {
        WKT
        {

            @Override
            public Geometry parse(String geomParam) throws ParseException
            {
                final WKTReader reader = new WKTReader();
                return reader.read(geomParam);
            }

        },
        GML2
        {

            Parser parser = new Parser(new GMLConfiguration());
            {
                parser.setFailOnValidationError(false);
                parser.setStrict(false);
                parser.setValidating(false);
            }

            @Override
            public Geometry parse(String geomParam) throws Exception
            {
                return gmlParsing(parser, geomParam);
            }

        },
        GML3
        {

            Parser parser = new Parser(new org.geotools.gml3.GMLConfiguration());
            {
                parser.setFailOnValidationError(false);
                parser.setStrict(false);
                parser.setValidating(false);
            }

            @Override
            public Geometry parse(String geomParam) throws Exception
            {
                return gmlParsing(parser, geomParam);
            }

        };

        public static Format lookup(String param)
        {
            for (final Format format : values()) {
                if (format.name().equals(param)) {
                    return format;
                }
            }
            throw new IllegalArgumentException(param + " is not a recognized format.  Choices include: "
                    + Arrays.toString(values()));
        }

        protected static Geometry gmlParsing(Parser parser, String gml) throws IOException, SAXException,
                ParserConfigurationException
        {
            Object obj = parser.parse(new StringReader(gml));

            if (obj instanceof Geometry) {
                return (Geometry) obj;
            }
            if (obj instanceof SimpleFeature) {
                return (Geometry) ((SimpleFeature) obj).getDefaultGeometry();
            }
            throw new AssertionError(obj.getClass().getName() + " was not an expected result from the Parser");
        }

        public abstract Geometry parse(String geomParam) throws Exception;
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        final GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final ExtentManager extentMan = gc.getExtentManager();

        String id = Util.getParamText(params, ID);
        final String wfsParam = Util.getParamText(params, SOURCE);
        final String typename = Util.getParamText(params, TYPENAME);
        final String geomParam = Util.getParamText(params, GEOM);
        final String geomId = LangUtils.createDescFromParams(params, GEO_ID);
        final String desc = LangUtils.createDescFromParams(params, DESC);
        final Format format = Format.lookup(Util.getParamText(params, FORMAT));
        final String requestCrsCode = Util.getParamText(params, ExtentHelper.CRS_PARAM);

        final Source wfs = extentMan.getSource(wfsParam);
        final FeatureType featureType = wfs.getFeatureType(typename);

        if (featureType == null) {
            return ExtentHelper.error(typename + " does not exist, acceptable types are: " + wfs.listModifiable());
        }

        if (requestCrsCode == null) {
            return ExtentHelper.error("the " + ExtentHelper.CRS_PARAM + " parameter is required");
        }

        if (!featureType.isModifiable()) {
            return ExtentHelper.error(typename + " is not a modifiable type, modifiable types are: "
                    + wfs.listModifiable());
        }
        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        if (id != null && idExists(store, id, featureType)) {
            return ExtentHelper.error("The id " + id + " already exists!");
        }

        Geometry geometry = format.parse(geomParam);
        id = add(id, geomId, desc, requestCrsCode, featureType, store, geometry, false);

        final Element responseElem = new Element("success");
        responseElem.setText("Added one new feature id= " + id);
        return responseElem;
    }

    public String add(String id, final String geoId, final String desc, final String requestCrsCode,
                      final FeatureType featureType, final FeatureStore<SimpleFeatureType, SimpleFeature> store, Geometry geometry, boolean showNative)
            throws Exception
    {
        final SimpleFeatureType schema = store.getSchema();
        geometry = ExtentHelper.prepareGeometry(requestCrsCode, featureType, geometry, schema);

        id = addFeature(id, geoId, desc, geometry, featureType, store, schema, showNative);
        return id;
    }

    static boolean idExists(FeatureStore<SimpleFeatureType, SimpleFeature> store, String id, FeatureType featureType)
            throws IOException
    {
        return !store.getFeatures(featureType.createQuery(id, new String[] { featureType.idColumn })).isEmpty();
    }

    private String addFeature(String id, String geoId, String desc, Geometry geometry, FeatureType featureType,
                              FeatureStore<SimpleFeatureType, SimpleFeature> store, SimpleFeatureType schema, boolean showNative) throws Exception
    {

        if (id == null) {
            id = ExtentHelper.findNextId(store, featureType);
        }

        final SimpleFeature feature = SimpleFeatureBuilder.template(schema, SimpleFeatureBuilder
                .createDefaultFeatureId());
        feature.setAttribute(featureType.idColumn, id);
        feature.setAttribute(featureType.geoIdColumn, encodeDescription(geoId));
        feature.setAttribute(featureType.descColumn, encodeDescription(desc));
        feature.setAttribute(featureType.showNativeColumn, showNative?"y":"n");
        feature.setAttribute(featureType.searchColumn, encodeDescription(reduceDesc(desc) + reduceDesc(geoId)));
        feature.setDefaultGeometry(geometry);

        final FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
        collection.add(feature);
        store.addFeatures(collection);
        return id;
    }

}
