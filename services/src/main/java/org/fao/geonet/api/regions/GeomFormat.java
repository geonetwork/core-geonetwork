/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.regions;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import org.fao.geonet.Constants;
import org.fao.geonet.csw.common.util.Xml;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.jdom.Element;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;

public enum GeomFormat {

    WKT {
        private final transient WKTWriter wktWriter = new WKTWriter();
        private final transient WKTReader wktReader = new WKTReader();

        @Override
        public Element toElement(Geometry geom) {
            return new Element("geom").setText(wktWriter.write(geom));
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            if (geomString.contains("%")) {
                geomString = URLDecoder.decode(geomString, Constants.ENCODING);
            }
            if (geomString.contains("+")) {
                geomString = geomString.replace("+", " ");
            }
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
            String gmlString = outputStream.toString(Constants.ENCODING);

            return Xml.loadString(gmlString, false);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Geometry parse(String geomString) throws Exception {
            geomString = decode(geomString);
            Object value;
            try {
                Parser parser3 = new Parser(gml3Config);
                value = parser3.parse(new StringReader(geomString));
            } catch (Exception e) {
                try {
                    Parser parser32 = new Parser(gml32Config);
                    value = parser32.parse(new StringReader(geomString));
                } catch (Exception e2) {
                    try {
                        Parser parser2 = new Parser(gml2Config);
                        value = parser2.parse(new StringReader(geomString));
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
            final Encoder encoder = new Encoder(gml2Config);
            encoder.setIndenting(false);
            encoder.setOmitXMLDeclaration(true);
            encoder.setNamespaceAware(true);

            encoder.encode(geom, org.geotools.gml2.GML.geometryMember, outputStream);
            String gmlString = outputStream.toString(Constants.ENCODING);

            return Xml.loadString(gmlString, false);
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            geomString = decode(geomString);
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
    GML32 {
        @Override
        public Element toElement(Geometry geom) throws Exception {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final Encoder encoder = new Encoder(gml3Config);
            encoder.setOmitXMLDeclaration(true);
            encoder.setNamespaceAware(true);
            encoder.setIndenting(false);

            encoder.encode(geom, org.geotools.gml3.v3_2.GML.geometryMember, outputStream);
            String gmlString = outputStream.toString(Constants.ENCODING);

            return Xml.loadString(gmlString, false);
        }

        @Override
        public Geometry parse(String geomString) throws Exception {
            geomString = decode(geomString);
            Object value;
            try {
                Parser parser32 = new Parser(gml32Config);
                value = parser32.parse(new StringReader(geomString));
            } catch (Exception e) {
                try {
                    Parser parser3 = new Parser(gml3Config);
                    value = parser3.parse(new StringReader(geomString));
                } catch (Exception e2) {
                    try {
                        Parser parser2 = new Parser(gml2Config);
                        value = parser2.parse(new StringReader(geomString));
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
    };

    static GMLConfiguration gml2Config = new GMLConfiguration();
    static org.geotools.gml3.GMLConfiguration gml3Config = new org.geotools.gml3.GMLConfiguration();
    static org.geotools.gml3.v3_2.GMLConfiguration gml32Config = new org.geotools.gml3.v3_2.GMLConfiguration();

    private static String decode(String geomString) throws UnsupportedEncodingException {
        if (!geomString.contains(" ")) {
            return URLDecoder.decode(geomString, Constants.ENCODING);
        }
        return geomString;
    }

    public static GeomFormat find(String geomType) {
        for (GeomFormat f : values()) {
            if (f.name().equalsIgnoreCase(geomType)) {
                return f;
            }
        }
        throw new IllegalArgumentException(geomType + " is not an acceptable format.  Permitted values are: " + Arrays.toString(values
            ()));
    }

    public abstract Element toElement(Geometry geom) throws Exception;

    public abstract Geometry parse(String geomString) throws Exception;
}
