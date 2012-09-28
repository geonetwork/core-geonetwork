//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.extent;

import static java.lang.String.format;
import static org.fao.geonet.services.extent.ExtentHelper.reducePrecision;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.extent.Source.FeatureType;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Encoder;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Obtains the geometry and description from the wfs (configured in config.xml)
 * and returns them as a gmd:extent xml fragment
 *
 * @author jeichar
 */
public class Get implements Service
{

    public enum Format
    {
        GMD_SPATIAL_EXTENT_POLYGON
        {
            @SuppressWarnings("unchecked")
            @Override
            public Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                    String extentTypeCode, CoordinateReferenceSystem crs) throws Exception
            {
                Element fullObject = get.gmdFormat(feature, GMD_POLYGON, featureType, extentTypeCode, crs);
                List<Element> geographicElements = new ArrayList<Element>(fullObject.getChildren("geographicElement",
                        GMD_NAMESPACE));

                Element polygon, bbox, id;
                polygon = bbox = id = null;

                for (Element element : geographicElements) {
                    element.detach();
                    element.setName("spatialExtent");
                    if (element.getChild("EX_BoundingPolygon", GMD_NAMESPACE) != null) {
                        polygon = element.getChild("EX_BoundingPolygon", GMD_NAMESPACE);
                    } else if (element.getChild("EX_GeographicBoundingBox", GMD_NAMESPACE) != null) {
                        bbox = element.getChild("EX_GeographicBoundingBox", GMD_NAMESPACE);
                    } else {
                        if (!element.getChildren().isEmpty()) {
                            id = (Element) element.getChildren().get(0);
                        }
                    }
                }

                if (polygon != null)
                    return polygon;
                if (bbox != null)
                    return bbox;
                return id;
            }
        },
        GMD_BBOX
        {
            @Override
            public Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                    String extentTypeCode, CoordinateReferenceSystem crs) throws Exception
            {
                return get.gmdFormat(feature, this, featureType, extentTypeCode, crs);
            }
        },
        GMD_POLYGON
        {
            @Override
            public Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                    String extentTypeCode, CoordinateReferenceSystem crs) throws Exception
            {
                return get.gmdFormat(feature, this, featureType, extentTypeCode, crs);
            }
        },
        GMD_COMPLETE
        {
            @Override
            public Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                    String extentTypeCode, CoordinateReferenceSystem crs) throws Exception
            {
                return get.gmdFormat(feature, this, featureType, extentTypeCode, crs);
            }
        },
        WKT
        {
            @Override
            public Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                    String extentTypeCode, CoordinateReferenceSystem crs) throws Exception
            {
                return get.formatWKT(feature, featureType, wfs, crs);
            }
        };

        public abstract Element format(Get get, SimpleFeature feature, FeatureType featureType, Source wfs,
                String extentTypeCode, CoordinateReferenceSystem crs) throws Exception;

        public static Format lookup(String formatParam)
        {
            Format format;
            if (formatParam == null) {
                format = Format.WKT;
            } else {
                format = Format.valueOf(formatParam.toUpperCase());
            }
            return format;
        }
    }

    private static final Namespace GMD_NAMESPACE    = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    private static final Namespace GCO_NAMESPACE    = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");

    private final GMLConfiguration gmlConfiguration = new GMLConfiguration();
    {
    	gmlConfiguration.getProperties().add(GMLConfiguration.NO_SRS_DIMENSION);
    }
    private String                 _appPath;

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        Util.toLowerCase(params);
        final GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final ExtentManager extentMan = gc.getExtentManager();

        final String id = Util.getParamText(params, ExtentHelper.ID);
        final String formatParam = Util.getParamText(params, ExtentHelper.FORMAT);
        final String wfsId = Util.getParamText(params, ExtentHelper.SOURCE);
        final String typename = Util.getParamText(params, ExtentHelper.TYPENAME);
        final String extentTypeCode = Util.getParamText(params, ExtentHelper.EXTENT_TYPE_CODE);
        final String epsgCode = Util.getParamText(params, ExtentHelper.CRS_PARAM);
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        if(epsgCode != null) {
        	crs = CRS.decode(epsgCode, true);
        }

        Format format = Format.lookup(formatParam);

        if (id == null) {
            ExtentHelper.error("id parameter is required");
        }

        if (typename == null) {
            ExtentHelper.error("typename parameter is required");
        }
        final Source wfs = extentMan.getSource(wfsId);
        final FeatureType featureType = wfs.getFeatureType(typename);
        if (featureType == null) {
            return errorTypename(extentMan, typename);
        }

        if (id==null || id.equals("SKIP") || id.length() == 0) {
            final Element response = new Element("response");
            formatFeatureType(featureType, wfs, response);
            return response;
        }

        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = featureType.getFeatureSource();

        String[] properties;
        if(featureSource.getSchema().getDescriptor(featureType.showNativeColumn) != null) {
            properties = new String[]{ featureType.idColumn, featureSource.getSchema().getGeometryDescriptor().getLocalName(),
                    featureType.descColumn, featureType.geoIdColumn, featureType.showNativeColumn };
        } else {
            properties = new String[]{ featureType.idColumn, featureSource.getSchema().getGeometryDescriptor().getLocalName(),
                    featureType.descColumn, featureType.geoIdColumn};
        }

        final FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        final Filter filter = filterFactory2.equals(filterFactory2.property(featureType.idColumn), filterFactory2
                .literal(id));

        final Query q = featureType.createQuery(filter,properties);

        final Element xml = resolve(format, id, featureSource, q, featureType, wfs, extentTypeCode, crs);
        return xml;
    }

    protected Element formatWKT(SimpleFeature next, FeatureType featureType, Source wfs, CoordinateReferenceSystem crs) throws Exception
    {
        final Element response = new Element("response");

        final Element featureTypeElem = formatFeatureType(featureType, wfs, response);

        final Element featureElem = new Element(ExtentHelper.FEATURE);
        final String id = next.getAttribute(featureType.idColumn).toString();
        featureElem.setAttribute(ExtentHelper.ID, id);
        featureTypeElem.addContent(featureElem);

        if (featureType.descColumn != null) {
            String desc = ExtentHelper.decodeDescription((String) next.getAttribute(featureType.descColumn));
            final Element descElem = Xml.loadString("<" + ExtentHelper.DESC + ">" + desc + "</" + ExtentHelper.DESC + ">", false);
            featureElem.addContent(descElem);
        }

        if (featureType.geoIdColumn != null) {
            String desc = ExtentHelper.decodeDescription((String) next.getAttribute(featureType.geoIdColumn));
            final Element descElem = Xml.loadString("<" + ExtentHelper.GEO_ID + ">" + desc + "</" + ExtentHelper.GEO_ID + ">", false);
            featureElem.addContent(descElem);
        }
        if (next.getDefaultGeometry() != null) {
            final Element geomElem = new Element(ExtentHelper.GEOM);
            final WKTWriter writer = new WKTWriter();
            Geometry geometry = (Geometry) next.getDefaultGeometry();
            MathTransform transform = CRS.findMathTransform(next.getFeatureType().getCoordinateReferenceSystem(), crs);
            Geometry transformed = JTS.transform(geometry, transform);
            final String wkt = writer.writeFormatted(reducePrecision(transformed, crs));
            String openLayersCompatibleWKT = wkt.replaceAll("\\s+", " ");
			geomElem.setText(openLayersCompatibleWKT);
            featureElem.addContent(geomElem);
        }
        return response;

    }

    private Element formatFeatureType(FeatureType featureType, Source wfs, Element response)
    {
        final Element wfsElem = new Element("wfs");
        wfsElem.setAttribute(ExtentHelper.ID, wfs.wfsId);
        response.addContent(wfsElem);

        final Element featureTypeElem = new Element(ExtentHelper.FEATURE_TYPE);
        featureTypeElem.setAttribute(ExtentHelper.TYPENAME, featureType.typename);
        featureTypeElem.setAttribute(ExtentHelper.ID_COLUMN, featureType.idColumn);
        featureTypeElem.setAttribute(ExtentHelper.DESC_COLUMN, featureType.descColumn);
        featureTypeElem.setAttribute(ExtentHelper.MODIFIABLE_FEATURE_TYPE, String.valueOf(featureType.isModifiable()));

        wfsElem.addContent(featureTypeElem);
        return featureTypeElem;
    }

    private Element resolve(Format format, String id, FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
            Query q, FeatureType featureType, Source wfs, String extentTypeCode, CoordinateReferenceSystem crs) throws Exception, Exception
    {
        final FeatureIterator<SimpleFeature> features = featureSource.getFeatures(q).features();
        try {
            if (features.hasNext()) {
                final SimpleFeature feature = features.next();

                return format.format(this, feature, featureType, wfs, extentTypeCode, crs);
            } else {
                return ExtentHelper.error("no features founds with ID=" + id);
            }
        } finally {
            features.close();
        }
    }

    private Element errorTypename(ExtentManager extentMan, String typename) throws IOException
    {
        final String options = Arrays.toString(extentMan.getDataStore().getTypeNames());
        final String msg = "Typename: " + typename + " does not exist.  Available options are: " + options;
        return ExtentHelper.error(msg);
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
        this._appPath = appPath;
    }

    private Element gmdFormat(SimpleFeature feature, Format format, FeatureType featureType, String extentTypeCode, CoordinateReferenceSystem crs)
            throws Exception
    {

        final Element exExtent = new Element("EX_Extent", GMD_NAMESPACE);
        final Element geographicElement = new Element("geographicElement", GMD_NAMESPACE);

        exExtent.addContent(geographicElement);
        Element geoExTypeEl;
        switch (format)
        {
        case GMD_BBOX:
            geoExTypeEl = bbox(feature,crs);
            geographicElement.addContent(geoExTypeEl);
            addExtentTypeCode(geoExTypeEl, extentTypeCode);

            break;
        case GMD_COMPLETE:
            geoExTypeEl = boundingPolygon(feature,crs);
            geographicElement.addContent(geoExTypeEl);
            addExtentTypeCode(geoExTypeEl, extentTypeCode);

            final Element geographicElement2 = new Element("geographicElement", GMD_NAMESPACE);
            exExtent.addContent(geographicElement2);
            Element bboxElem = bbox(feature,crs);
            geographicElement2.addContent(bboxElem);
            addExtentTypeCode(bboxElem, extentTypeCode);

            break;
        case GMD_POLYGON:
            geoExTypeEl = boundingPolygon(feature,crs);
            geographicElement.addContent(geoExTypeEl);
            addExtentTypeCode(geoExTypeEl, extentTypeCode);

            break;

        default:
            throw new IllegalArgumentException(format + " is not one of the permitted formats for this method");
        }

        String attribute = (String) feature.getAttribute(featureType.geoIdColumn);
        Element geoIdElem = createGeoIdElem(attribute);
        if (geoIdElem != null) {
            exExtent.addContent(0, geoIdElem);
        }

        try {
            Element descElem = null;
            attribute = (String) feature.getAttribute(featureType.descColumn);
            if (attribute != null) {
                descElem = LangUtils.toIsoMultiLingualElem(_appPath, ExtentHelper.decodeDescription(attribute));
            }
            if (descElem == null) {
                // making a desc object always present. Mostly a hack to make
                // editing easier
                descElem = LangUtils.toIsoMultiLingualElem(_appPath, " ");
            }
            exExtent.addContent(0, descElem);
        } catch (final Exception e) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            Log.error("org.fao.geonet.services.xlink.Extent", "Error parsing XML from feature:\n" + out);
        }

        return exExtent;
    }

    private void addExtentTypeCode(final Element parentEl, String extentTypeCode)
    {
        final Element typeCodeEl = new Element("extentTypeCode", GMD_NAMESPACE);
        final Element booleanEl = new Element("Boolean", GCO_NAMESPACE);

        parentEl.addContent(0, typeCodeEl);
        typeCodeEl.addContent(booleanEl);

        if (extentTypeCode == null || extentTypeCode.trim().length() == 0) {
            extentTypeCode = "true";
        }
        if(extentTypeCode.trim().equals("1")) {
            extentTypeCode = "true";
        }
        Boolean value = Boolean.parseBoolean(extentTypeCode.trim());
        booleanEl.setText(value?"1":"0");
    }

    @SuppressWarnings("unchecked")
    private Element createGeoIdElem(String attribute) throws Exception
    {

        Element geoEl = new Element("geographicElement", GMD_NAMESPACE);
        Element geoDesEl = new Element("EX_GeographicDescription", GMD_NAMESPACE);
        Element geoIdEl = new Element("geographicIdentifier", GMD_NAMESPACE);
        Element mdIdEl = new Element("MD_Identifier", GMD_NAMESPACE);
        Element codeEl = new Element("code", GMD_NAMESPACE);

        geoEl.addContent(geoDesEl);

        geoDesEl.addContent(geoIdEl);
        geoIdEl.addContent(mdIdEl);
        mdIdEl.addContent(codeEl);

        if (attribute != null && attribute.trim().length() > 0) {
        codeEl.setAttribute("type", "gmd:PT_FreeText_PropertyType", Namespace.getNamespace("xsi",
                "http://www.w3.org/2001/XMLSchema-instance"));
        String decodeDescription = ExtentHelper.decodeDescription(attribute);

        List<Content> content = new ArrayList<Content>(LangUtils.toIsoMultiLingualElem(_appPath,
                decodeDescription).getContent());
        for (Content element : content) {
            element.detach();
            codeEl.addContent(element);
        }
        } else {
            return null;
        }

        return geoEl;
    }

    private Element bbox(SimpleFeature feature, CoordinateReferenceSystem crs) throws Exception
    {

        final Object showNativeAtt = feature.getAttribute(FeatureType.SHOW_NATIVE);
        boolean showNative = showNativeAtt != null && showNativeAtt.equals("y");

        Element bbox = new Element("EX_GeographicBoundingBox", GMD_NAMESPACE);
        Element west = new Element("westBoundLongitude", GMD_NAMESPACE);
        Element east = new Element("eastBoundLongitude", GMD_NAMESPACE);
        Element south = new Element("southBoundLatitude", GMD_NAMESPACE);
        Element north = new Element("northBoundLatitude", GMD_NAMESPACE);

        BoundingBox bounds = feature.getBounds();
        double eastDecimal = reducePrecision(bounds.getMaxX(), 0);
        double westDecimal = reducePrecision(bounds.getMinX(), 0);
        double southDecimal = reducePrecision(bounds.getMinY(), 0);
        double northDecimal = reducePrecision(bounds.getMaxY(), 0);

        if(showNative) {
            bbox.addContent(new Comment(format("native coords: %s,%s,%s,%s", westDecimal, southDecimal, eastDecimal, northDecimal)));
        }

        bbox.addContent(west);
        bbox.addContent(east);
        bbox.addContent(south);
        bbox.addContent(north);

        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        MathTransform transform = CRS.findMathTransform(feature.getFeatureType().getCoordinateReferenceSystem(), crs);
        Geometry transformed = JTS.transform(geometry, transform);

        Envelope latLongBounds = transformed.getEnvelopeInternal();
        double latLongEastDecimal = reducePrecision(latLongBounds.getMaxX(), ExtentHelper.COORD_DIGITS);
        double latLongWestDecimal = reducePrecision(latLongBounds.getMinX(), ExtentHelper.COORD_DIGITS);
        double latLongSouthDecimal = reducePrecision(latLongBounds.getMinY(), ExtentHelper.COORD_DIGITS);
        double latLongNorthDecimal = reducePrecision(latLongBounds.getMaxY(), ExtentHelper.COORD_DIGITS);

        west.addContent(decimal(latLongWestDecimal));
        east.addContent(decimal(latLongEastDecimal));
        south.addContent(decimal(latLongSouthDecimal));
        north.addContent(decimal(latLongNorthDecimal));

        return bbox;
    }

    private Element decimal(double value)
    {
        Element dec = new Element("Decimal", GCO_NAMESPACE);
        dec.setText(String.valueOf(value));
        return dec;
    }

    private Element boundingPolygon(SimpleFeature feature, CoordinateReferenceSystem crs) throws Exception
    {
        final Element boundingPoly = new Element("EX_BoundingPolygon", GMD_NAMESPACE);
        final Element polyon = new Element("polygon", GMD_NAMESPACE);
        final Element geom = encodeAsGML(feature, crs);
        geom.detach();
        boundingPoly.addContent(polyon);
        polyon.addContent(geom);
        return boundingPoly;
    }

    private Element encodeAsGML(SimpleFeature feature, CoordinateReferenceSystem crs) throws Exception
    {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Encoder encoder = new Encoder(gmlConfiguration);
        encoder.setIndenting(false);
        final CoordinateReferenceSystem baseCrs = feature.getFeatureType().getCoordinateReferenceSystem();
        MathTransform transform = CRS.findMathTransform(baseCrs, crs, true);
        Geometry transformed = JTS.transform((Geometry) feature.getDefaultGeometry(), transform );
        reducePrecision(transformed,crs);

        ExtentHelper.addGmlId(transformed);
        encoder.encode(transformed, org.geotools.gml3.GML.geometryMember, outputStream);
        String gmlString = outputStream.toString();
		Element geometryMembers = Xml.loadString(gmlString, false);
        @SuppressWarnings("rawtypes")
		Iterator iter = geometryMembers.getChildren().iterator();
        do {
            Object next = iter.next();
            if (next instanceof Element) {
                return (Element) next;
            }
        } while (iter.hasNext());

        throw new RuntimeException(transform+ " was not encoded correctly to GML");
    }

}
