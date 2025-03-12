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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
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

    public static MultiPolygon parseGml(Parser parser, String gml) throws IOException, SAXException,
        ParserConfigurationException {
        Object value = parser.parse(new StringReader(gml));
        if (value instanceof HashMap) {
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

        } else if (value == null) {
            return null;
        } else {
            return toMultiPolygon((Geometry) value);
        }
    }

    public static MultiPolygon toMultiPolygon(Geometry geometry) {
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;

            return geometry.getFactory().createMultiPolygon(
                new Polygon[]{polygon});
        } else if (geometry instanceof MultiPolygon) {
            return (MultiPolygon) geometry;
        }
        String message = geometry.getClass() + " cannot be converted to a polygon. Check metadata";
        Log.error(Geonet.INDEX_ENGINE, message);
        throw new IllegalArgumentException(message);
    }

    public static void addToList(List<MultiPolygon> geoms, Object entry) {
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

}
