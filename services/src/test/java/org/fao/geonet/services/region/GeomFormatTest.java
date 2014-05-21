package org.fao.geonet.services.region;

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