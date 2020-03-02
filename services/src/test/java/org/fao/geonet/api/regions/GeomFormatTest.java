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

import org.geotools.xml.Encoder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;

import static org.junit.Assert.assertNotNull;

/**
 * Add Tests for parsing geometries coming in via get.
 * <p/>
 * Created by Jesse on 3/17/14.
 */
public class GeomFormatTest {
    @Test
    public void testParseWkt() throws Exception {
        final String geomString = "Polygon((482200.0%2063000.0,835700.0%2063000.0,835700.0%20301000.0,482200.0%20301000.0," +
            "482200.0%2063000.0))";
        assertNotNull(GeomFormat.WKT.parse(geomString));
    }

    @Test
    public void testParseWkt2() throws Exception {
        final String geomString = "Polygon((482200.0+63000.0,835700.0+63000.0,835700.0+301000.0,482200.0+301000.0,482200.0+63000.0))";
        assertNotNull(GeomFormat.WKT.parse(geomString));
    }

    @Test
    public void testGML2() throws Exception {
        final Geometry geometry = GeomFormat.WKT.parse("Polygon((482200.0 63000.0,835700.0 63000.0,835700.0 301000.0,482200.0 301000.0," +
            "482200.0 63000.0))");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Encoder encoder = new Encoder(GeomFormat.gml2Config);
        encoder.setIndenting(false);
        encoder.setOmitXMLDeclaration(true);
        encoder.setNamespaceAware(true);

        encoder.encode(geometry, org.geotools.gml2.GML.geometryMember, outputStream);
        String gmlString = outputStream.toString();

        assertNotNull(GeomFormat.GML2.parse(gmlString));
        assertNotNull(GeomFormat.GML2.parse(URLEncoder.encode(gmlString, "UTF-8")));
    }

    @Test
    public void testGML3() throws Exception {
        final Geometry geometry = GeomFormat.WKT.parse("Polygon((482200.0 63000.0,835700.0 63000.0,835700.0 301000.0,482200.0 301000.0," +
            "482200.0 63000.0))");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Encoder encoder = new Encoder(GeomFormat.gml3Config);
        encoder.setIndenting(false);
        encoder.setOmitXMLDeclaration(true);
        encoder.setNamespaceAware(true);

        encoder.encode(geometry, org.geotools.gml3.GML.geometryMember, outputStream);
        String gmlString = outputStream.toString();

        assertNotNull(GeomFormat.GML3.parse(gmlString));
        assertNotNull(GeomFormat.GML3.parse(URLEncoder.encode(gmlString, "UTF-8")));
    }

    @Test
    public void testGML32() throws Exception {
        final Geometry geometry = GeomFormat.WKT.parse("Polygon((482200.0 63000.0,835700.0 63000.0,835700.0 301000.0,482200.0 301000.0," +
            "482200.0 63000.0))");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Encoder encoder = new Encoder(GeomFormat.gml32Config);
        encoder.setIndenting(false);
        encoder.setOmitXMLDeclaration(true);
        encoder.setNamespaceAware(true);

        encoder.encode(geometry, org.geotools.gml3.v3_2.GML.geometryMember, outputStream);
        String gmlString = outputStream.toString();

        assertNotNull(GeomFormat.GML32.parse(gmlString));
        assertNotNull(GeomFormat.GML32.parse(URLEncoder.encode(gmlString, "UTF-8")));
    }
}
