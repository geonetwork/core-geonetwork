package org.fao.geonet.api.records.extent;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.fao.geonet.kernel.region.Region;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapRendererTest {

    private final WKTWriter wktWriter = new WKTWriter();
    private final WKTReader wktReader = new WKTReader();

    @Test
    public void domainContainsBoundingBox() throws Exception {
        String test = "POLYGON ((40 -63, 40 -20, 70 -20, 70 -63, 40 -63))";
        Geometry bbox = wktReader.read(test);
        bbox.setSRID(4326);
        String result = wktWriter.write(MapRenderer.computeGeomInDomainOfValidity(bbox, Region.decodeCRS("EPSG:3857")));
        assertEquals(test, result);
    }

    @Test
    public void domainIntersectsBoundingBox() throws Exception {
        String test = "POLYGON ((40 -86, 40 -20, 70 -20, 70 -86, 40 -86))";
        Geometry bbox = wktReader.read(test);
        bbox.setSRID(4326);
        String result = wktWriter.write(MapRenderer.computeGeomInDomainOfValidity(bbox, Region.decodeCRS("EPSG:3857")));
        assertEquals("POLYGON ((40 -85.06, 40 -20, 70 -20, 70 -85.06, 40 -85.06))", result);
    }

    @Test
    public void domainContainsBoundingPolygon() throws Exception {
        String test = "POLYGON ((165 -83, 135 -76, 161 -76, 153 -81, 165 -83))";
        Geometry bbox = wktReader.read(test);
        bbox.setSRID(4326);
        String result = wktWriter.write(MapRenderer.computeGeomInDomainOfValidity(bbox, Region.decodeCRS("EPSG:3857")));
        assertEquals("POLYGON ((165 -83, 135 -76, 161 -76, 153 -81, 165 -83))", result);
    }

    @Test
    public void domainIntersectsBoundingPolygon() throws Exception {
        String test = "POLYGON ((165 -87, 135 -76, 161 -76, 153 -81, 165 -87))";
        Geometry bbox = wktReader.read(test);
        bbox.setSRID(4326);
        String result = wktWriter.write(MapRenderer.computeGeomInDomainOfValidity(bbox, Region.decodeCRS("EPSG:3857")));
        assertEquals("POLYGON ((159.70909090909092 -85.06, 135 -76, 161 -76, 153 -81, 161.12 -85.06, 159.70909090909092 -85.06))", result);
    }
}
