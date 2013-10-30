package org.fao.geonet.services.extent;

import com.vividsolutions.jts.geom.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test methods in Get
 * User: Jesse
 * Date: 10/29/13
 * Time: 5:06 PM
 */
public class GetTest {
    GeometryFactory factory = new GeometryFactory();

    @Test
    public void testRemoveDuplicatePointsMultiPolygon() throws Exception {
        Geometry geometry = Get.removeDuplicatePoints(factory.createMultiPolygon(new Polygon[0]));
        assertEquals(0, geometry.getNumGeometries());


        Polygon[] polygons = new Polygon[]{
                createPolygon(),
                createPolygon()
        };
        final MultiPolygon multiPolygon = factory.createMultiPolygon(polygons);

        geometry = Get.removeDuplicatePoints(multiPolygon);

        assertEquals(2, geometry.getNumGeometries());
        assertTrue(geometry.getGeometryN(0).toText()+" contains duplicates", noDuplicates(geometry.getGeometryN(0)));
        assertTrue(geometry.getGeometryN(1).toText()+" contains duplicates", noDuplicates(geometry.getGeometryN(1)));
    }

    @Test
    public void testRemoveDuplicatePointsLineString() throws Exception {
        final LineString lineString = factory.createLineString(createLinearRing(0, 10).getCoordinates());

        assertTrue(lineString.toText()+" contains duplicates",noDuplicates(Get.removeDuplicatePoints(lineString)));
    }

    @Test
    public void testRemoveDuplicatePointsPoint() throws Exception {
        final Point point = factory.createPoint(new Coordinate(1,2));

        assertTrue(point.equalsExact(Get.removeDuplicatePoints(point)));
    }

    private boolean noDuplicates(Geometry geom) {
        boolean duplicates = false;

        Coordinate[] coords = geom.getCoordinates();
        Coordinate last = geom.getCoordinates()[0];

        for (int i = 1; i < coords.length; i++) {
            Coordinate coord = coords[i];

            duplicates |= last.equals(coord);
            last = coord;
        }
        return !duplicates;
    }

    private Polygon createPolygon() {
        LinearRing shell = createLinearRing(0, 20);
        LinearRing[] rings = new LinearRing[]{
                createLinearRing(2, 1),
                createLinearRing(4, 1)
        };

        return factory.createPolygon(shell, rings);
    }

    private LinearRing createLinearRing(int i, int j) {
        return factory.createLinearRing(new Coordinate[]{
                new Coordinate(i, i),
                new Coordinate(i + j, i),
                new Coordinate(i + j, i + j),
                new Coordinate(i + j, i + j),
                new Coordinate(i, i + j),
                new Coordinate(i, i)
        });
    }
}
