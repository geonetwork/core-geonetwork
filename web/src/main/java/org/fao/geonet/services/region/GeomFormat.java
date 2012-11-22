package org.fao.geonet.services.region;

import java.io.StringReader;
import java.util.HashMap;

import org.fao.geonet.csw.common.util.Xml;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Parser;
import org.jdom.Element;

import scala.actors.threadpool.Arrays;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public enum GeomFormat {

    WKT{
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
    GML {
        GMLConfiguration gml2Config = new GMLConfiguration();
        org.geotools.gml3.GMLConfiguration gml3Config = new org.geotools.gml3.GMLConfiguration();
        org.geotools.gml3.v3_2.GMLConfiguration gml32Config = new org.geotools.gml3.v3_2.GMLConfiguration();
        GMLWriter writer = new GMLWriter(true);
        @Override
        public Element toElement(Geometry geom) throws Exception {
            return Xml.loadString(writer.write(geom), true);
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
                return (Geometry) ((HashMap)value).values().iterator().next();
            } else {
                return (Geometry) value;
            }
        }
    };
    
    public abstract Element toElement(Geometry geom) throws Exception;
    public abstract Geometry parse(String geomString) throws Exception;

    public static GeomFormat find(String geomType) {
        for (GeomFormat f : values()) {
            if(f.name().equalsIgnoreCase(geomType)) {
                return f;
            }
        }
        throw new IllegalArgumentException(geomType+" is not an acceptable format.  Permitted values are: "+Arrays.toString(values()));
    }
}
