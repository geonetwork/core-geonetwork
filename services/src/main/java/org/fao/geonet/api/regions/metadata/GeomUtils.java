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

/**
 * Utility class to help with geometry and gml.
 */
public class GeomUtils {

    public static Geometry getSpatialExtent(Path schemaDir, Element metadata, ErrorHandler errorHandler) throws Exception {
        org.geotools.util.logging.Logging.getLogger("org.geotools.xml")
            .setLevel(Level.SEVERE);
        Path sSheet = schemaDir.resolve("extract-gml.xsl").toAbsolutePath();
        Element transform = Xml.transform(metadata, sSheet);
        if (transform.getChildren().size() == 0) {
            return null;
        }
        List<Geometry> allGeometry = new ArrayList<Geometry>();
        for (Element geom : (List<Element>) transform.getChildren()) {
            Parser parser = GMLParsers.create(geom);
            String srs = geom.getAttributeValue("srsName");
            CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
            String gml = Xml.getString(geom);

            try {
                if (srs != null && !(srs.equals(""))) sourceCRS = CRS.decode(srs);
                Geometry geometry = parseGml(parser, gml);

                // if we have an srs and its not WGS84 then transform to WGS84
                if (!CRS.equalsIgnoreMetadata(sourceCRS, DefaultGeographicCRS.WGS84)) {
                    MathTransform tform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84);
                    geometry = JTS.transform(geometry, tform);
                }
                allGeometry.add(geometry);
            } catch (Exception e) {
                errorHandler.handleParseException(e, gml);
                // continue
            }
        }

        try {
            return toGeometry(allGeometry);
        } catch (Exception e) {
            errorHandler.handleBuildException(e, allGeometry);
            return null; // continue
        }
    }

    /**
     * Process list into a single geometry, choosing the most appropriate geometry collection.
     *
     * <ul>
     *     <li>f the list is empty geometry is {@code null}</li>
     *     <li>If list contains a single geometry it is returned</li>
     *     <li>If the list contains all Points, a MultiPoint will be created</li>
     *     <li>If the list contains all LineStrings a MultiLineString will be created</li>
     *     <li>If the list contains all Polygons a MultiPolygon is created</li>
     *     <li>If the list contains mixed contents a GeometryCollection is created</li>
     * </ul>
     *
     * @param geoms list of geometry to process
     * @return geometry, or geometry collection as required, or null if list is empty
     */
    protected static Geometry toGeometry(List<Geometry> geoms){
        if( geoms == null || geoms.isEmpty()){
            return null;
        }
        if( geoms.size()==1 ){
            return geoms.get(0);
        }

        GeometryFactory factory = geoms.get(0).getFactory();

        // determine geometry collection to create
        int dimension = -2; // mixed content geometry collection
        for( Geometry geom : geoms){
            if( geom instanceof GeometryCollection){
                dimension = -1;
                break; // mixed content geometry collection
            }

            if( dimension == -2 ) {
                dimension = geom.getDimension();
            }
            else if (dimension != geom.getDimension()){
                dimension = -1;
                break; // mixed content geometry collection
            }
        }

        // process list into geometry collection
        switch (dimension){
        case 0:
            return factory.createMultiPoint( geoms.toArray(new Point[geoms.size()]));
        case 1:
            return factory.createMultiLineString( geoms.toArray(new LineString[geoms.size()]) );
        case 2:
            return factory.createMultiPolygon( geoms.toArray(new Polygon[geoms.size()]) );
        default:
            return factory.createGeometryCollection( geoms.toArray(new Geometry[geoms.size()]) );
        }
    }

    /**
     * Parse GML into a geometry: polygons, linestring, point or appropriate geometry collection as required.
     *
     * The resulting geometry may be clipped or split to accommodate spatial reference system bounds. Points are not buffered
     * in any way (so the bounds of a point will have width and height zero).
     *
     * @param parser
     * @param gml
     * @return geometry, or null if not provided.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Geometry parseGml(Parser parser, String gml) throws IOException, SAXException,
        ParserConfigurationException {
        Object value = parser.parse(new StringReader(gml));

        if( value == null ){
            return null;
        }
        else if (value instanceof HashMap) {
            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;
            List<Geometry> geoms = new ArrayList<Geometry>();
            for (Object entry : map.values()) {
                addToList(geoms, entry);
            }
            return toGeometry( geoms );
        } else if (value instanceof Geometry ){
            return (Geometry) value;
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

    /**
     * Process entry and add any geometries to list.
     *
     * The contents of any GeometryCollection (such as MultiPolygon) are added one-by-one
     * by one to the list.
     */
    protected static final void addToList(List<Geometry> geoms, Object entry) {
        if (entry instanceof Geometry) {
            Geometry geom = (Geometry) entry;
            if( geom instanceof GeometryCollection){
                GeometryCollection collection = (GeometryCollection) geom;
                for( int i=0; i<collection.getNumGeometries();i++){
                    addToList( geoms, collection.getGeometryN(i));
                }
            }
            else {
                geoms.add(geom);
            }
        } else if (entry instanceof Collection) {
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) entry;
            for (Object object : collection) {
                addToList(geoms,object);
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
