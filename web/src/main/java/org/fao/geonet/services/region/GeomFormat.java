package org.fao.geonet.services.region;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;

import org.fao.geonet.csw.common.util.Xml;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.jdom.Element;

import scala.actors.threadpool.Arrays;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public enum GeomFormat {

    WKT {
        private WKTWriter wktWriter = new WKTWriter();
        private WKTReader wktReader = new WKTReader();

        @Override
        public Element toElement(Geometry geom) {
            return new Element("geom").setText(wktWriter.write(geom));
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            return wktReader.read(geomString);
        }
    },
    GML3 {
        @Override
        public Element toElement(Geometry geom) throws Exception {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Encoder encoder = new Encoder(gml3Config);
            encoder.setIndenting(false);
            encoder.setOmitXMLDeclaration(true);
            encoder.setNamespaceAware(true);

            encoder.encode(geom, org.geotools.gml3.GML.geometryMember, outputStream);
            String gmlString = outputStream.toString();

            return Xml.loadString(gmlString, false);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Geometry parse(String geomString) throws Exception {
            Object value;
            try {
                Parser parser2 = new Parser(gml2Config);
                value = parser2.parse(new StringReader(geomString));
            } catch (Exception e) {
                try {
                    Parser parser3 = new Parser(gml3Config);
                    value = parser3.parse(new StringReader(geomString));
                } catch (Exception e2) {
                    try {
                        Parser parser32 = new Parser(gml32Config);
                        value = parser32.parse(new StringReader(geomString));
                    } catch (Exception e3) {
                        throw e;
                    }
                }
            }
            if (value instanceof HashMap) {
                return (Geometry) ((HashMap) value).values().iterator().next();
            } else {
                return (Geometry) value;
            }
        }
    },
    GML2 {
        @Override
        public Element toElement(Geometry geom) throws Exception {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Encoder encoder = new Encoder(GML2.gml3Config);
            encoder.setIndenting(false);
            encoder.setOmitXMLDeclaration(true);
            encoder.setNamespaceAware(true);

            encoder.encode(geom, org.geotools.gml2.GML.geometryMember, outputStream);
            String gmlString = outputStream.toString();

            return Xml.loadString(gmlString, false);
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            return GML3.parse(geomString);
        }
    },
    GML32 {
        @Override
        public Element toElement(Geometry geom) throws Exception {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Encoder encoder = new Encoder(GML32.gml3Config);
            encoder.setOmitXMLDeclaration(true);
            encoder.setNamespaceAware(true);
            encoder.setIndenting(false);

            encoder.encode(geom, org.geotools.gml3.v3_2.GML.geometryMember, outputStream);
            String gmlString = outputStream.toString();

            return Xml.loadString(gmlString, false);
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            return GML3.parse(geomString);
        }
    };

    public abstract Element toElement(Geometry geom) throws Exception;

    public abstract Geometry parse(String geomString) throws Exception;

    GMLConfiguration gml2Config = new GMLConfiguration();
    org.geotools.gml3.GMLConfiguration gml3Config = new org.geotools.gml3.GMLConfiguration();
    org.geotools.gml3.v3_2.GMLConfiguration gml32Config = new org.geotools.gml3.v3_2.GMLConfiguration();

    public static GeomFormat find(String geomType) {
        for (GeomFormat f : values()) {
            if (f.name().equalsIgnoreCase(geomType)) {
                return f;
            }
        }
        throw new IllegalArgumentException(geomType + " is not an acceptable format.  Permitted values are: " + Arrays.toString(values()));
    }
}
