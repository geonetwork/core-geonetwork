package org.fao.geonet.api.regions.metadata;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.ErrorHandler;
import org.fao.geonet.util.GMLParsers;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xsd.Parser;
import org.jdom.Element;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class GeomUtils {
    public static MultiPolygon getSpatialExtent(Path schemaDir, Element metadata, ErrorHandler errorHandler) throws Exception {
        org.geotools.util.logging.Logging.getLogger("org.geotools.xml")
            .setLevel(Level.SEVERE);
        Path sSheet = schemaDir.resolve("extract-gml.xsl").toAbsolutePath();
        Element transform = Xml.transform(metadata, sSheet);
        if (transform.getChildren().size() == 0) {
            return null;
        }
        List<Polygon> allPolygons = new ArrayList<Polygon>();
        for (Element geom : (List<Element>) transform.getChildren()) {
            Parser parser = GMLParsers.create(geom);
            String srs = geom.getAttributeValue("srsName");
            CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
            String gml = Xml.getString(geom);

            try {
                if (srs != null && !(srs.equals(""))) sourceCRS = CRS.decode(srs);
                MultiPolygon jts = parseGml(parser, gml);

                // if we have an srs and its not WGS84 then transform to WGS84
                if (!CRS.equalsIgnoreMetadata(sourceCRS, DefaultGeographicCRS.WGS84)) {
                    MathTransform tform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84);
                    jts = (MultiPolygon) JTS.transform(jts, tform);
                }

                for (int i = 0; i < jts.getNumGeometries(); i++) {
                    allPolygons.add((Polygon) jts.getGeometryN(i));
                }
            } catch (Exception e) {
                errorHandler.handleParseException(e, gml);
                // continue
            }
        }

        if (allPolygons.isEmpty()) {
            return null;
        } else {
            try {
                Polygon[] array = new Polygon[allPolygons.size()];
                GeometryFactory geometryFactory = allPolygons.get(0).getFactory();
                return geometryFactory.createMultiPolygon(allPolygons.toArray(array));


            } catch (Exception e) {
                errorHandler.handleBuildException(e, allPolygons);
                // continue
                return null;
            }
        }
    }

    /**
     * Parse GML into a bounding multi-polygon, polygons represented as is, linestring and points buffered to civic scale.
     *
     * Aside: The resulting multi-polygon may be clipped or split to accommodate spatial reference system bounds.
     *
     * @param parser
     * @param gml
     * @return bounding multi-polygon for gml geometry
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static MultiPolygon parseGml(Parser parser, String gml) throws IOException, SAXException,
        ParserConfigurationException {
        Object value = parser.parse(new StringReader(gml));

        if( value == null ){
            return null;
        }
        else if (value instanceof HashMap) {
            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;
            List<MultiPolygon> geoms = new ArrayList<MultiPolygon>();
            for (Object entry : map.values()) {
                addToList(geoms, entry);
            }
            if (geoms.isEmpty()) {
                return null;
            } else if (geoms.size() > 1) {
                GeometryFactory factory = geoms.get(0).getFactory();
                return factory.createMultiPolygon(geoms.toArray(new Polygon[0]));
            } else {
                return toMultiPolygon(geoms.get(0));
            }
        } else if (value instanceof Geometry ){
            return toMultiPolygon((Geometry) value);
        }
        else {
            return null;
        }
    }

    /**
     * Produces a multipolygon that covers the provided geometry.
     *
     * @param geometry
     * @return covering multipolygon
     */
    public static MultiPolygon toMultiPolygon(Geometry geometry) {
        if (geometry == null) return null;
        if (geometry instanceof MultiPolygon) {
            return (MultiPolygon) geometry;
        }
        final double DISTANCE = 0.01;
        GeometryTransformer transform = new CoveredByTransformer(DISTANCE);

        Geometry transformed = transform.transform(geometry);
        if( transformed == null ){
            String message = geometry.getClass() + " cannot be converted to a polygon. Check metadata";
            Log.error(Geonet.INDEX_ENGINE, message);
            throw new IllegalArgumentException(message);
        }
        transformed.setSRID(geometry.getSRID());
        transformed.setUserData(geometry.getUserData());

        if( transformed instanceof  MultiPolygon) {
            return (MultiPolygon) transformed;
        }
        else {
            return null;
        }
    }

    /** Process entry and add to list */
    protected static final void addToList(List<MultiPolygon> geoms, Object entry) {
        if (entry instanceof Polygon) {
            geoms.add(toMultiPolygon((Polygon) entry));
        } else if (entry instanceof MultiPolygon) {
            geoms.add((MultiPolygon) entry);
        } else if (entry instanceof Collection) {
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) entry;
            for (Object object : collection) {
                geoms.add(toMultiPolygon((Polygon) object));
            }
        }
    }

    /**
     * GeometryTransformer using buffer and bbox to return a MultiPolygon
     * covering the provided geometry.
     *
     * The generated geometry satisfies a {@link Geometry#coveredBy(Geometry)} relationship,
     * and can acts as a "bounding polygon" to index the provided geometry.
     */
    private static class CoveredByTransformer extends GeometryTransformer {

        private final double DISTANCE;

        public CoveredByTransformer(double DISTANCE) {
            this.DISTANCE = DISTANCE;
        }

        protected Geometry checkChildPolygon(Polygon polygon, Geometry parent ){
            if( parent instanceof GeometryCollection){
                // MultiLineString, MultiPoint, MultiPolygon or GeometryCollection ..
                return polygon;
            }
            else {
                return factory.createMultiPolygon(new Polygon[]{polygon});
            }
        }

        @Override
        protected Geometry transformLinearRing(LinearRing linearRing, Geometry parent) {
            if(parent instanceof Polygon) {
                // used as exterior or interior ring forming a polygon
                return super.transformLinearRing(linearRing, parent);
            }

            Polygon polygon = factory.createPolygon(linearRing);
            Geometry bounds = polygon.buffer(DISTANCE);

            if( bounds instanceof Polygon) {
                return checkChildPolygon((Polygon) polygon, parent);
            }
            return null;
        }

        @Override
        protected Geometry transformLineString(LineString lineString, Geometry parent) {
            Geometry bounds = lineString.buffer(DISTANCE);
            if( bounds instanceof Polygon) {
                return checkChildPolygon((Polygon) bounds, parent);
            }
            return null;
        }

        @Override
        protected Geometry transformPolygon(Polygon polygon, Geometry parent) {
            return checkChildPolygon( polygon, parent );
        }

        protected Geometry transformPoint(Point point, Geometry parent) {
            if (point.isEmpty()){
                return null; // skip
            }
            Geometry bounds = point.buffer(DISTANCE);

            if( bounds instanceof Polygon) {
                return checkChildPolygon( (Polygon) bounds, parent );
            }
            return null;
        }

        protected Geometry transformGeometryCollection(GeometryCollection geom, Geometry parent) {
            List<Polygon> transGeomList = new ArrayList<>();
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                Geometry transformGeom = transform(geom.getGeometryN(i));
                if (transformGeom == null) continue;
                if (transformGeom.isEmpty()) continue;

                if( transformGeom instanceof MultiPolygon) {
                    MultiPolygon transformedMultiPolygon = (MultiPolygon) transformGeom;
                    for (int j = 0; j < transformedMultiPolygon.getNumGeometries(); j++) {
                        Polygon polygon = (Polygon) transformedMultiPolygon.getGeometryN(j);
                        if (polygon == null) continue;
                        if (polygon.isEmpty()) continue;
                        transGeomList.add(polygon);
                    }
                }
                else if (transformGeom instanceof Polygon){
                    transGeomList.add((Polygon)transformGeom);
                }
            }
            return factory.createMultiPolygon(GeometryFactory.toPolygonArray(transGeomList));
        }
    }

}
