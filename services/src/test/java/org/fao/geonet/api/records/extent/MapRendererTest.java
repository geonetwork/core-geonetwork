/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.extent;

import org.fao.geonet.kernel.region.Region;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
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

    @Test
    public void extentGeodesicLocalProjection() throws Exception {
        String test = "POLYGON ((646.3563610491983 308975.2885578188, 10545.528150276805 637111.0281460616, 276050.8102636032 636456.3084312443, 284347.2430806639 308289.5622343737, 646.3563610491983 308975.2885578188))";
        String localSrs = "EPSG:28992";
        Geometry geometry = wktReader.read(test);
        geometry.setSRID(28992);

        Geometry geometryExtent = MapRenderer.getGeometryExtent(geometry, localSrs, true);
        assertEquals(geometry, geometryExtent);
    }

    @Test
    public void extentNonGeodesicLocalProjection() throws Exception {
        String test = "POLYGON ((646.3563610491983 308975.2885578188, 10545.528150276805 637111.0281460616, 276050.8102636032 636456.3084312443, 284347.2430806639 308289.5622343737, 646.3563610491983 308975.2885578188))";
        String localSrs = "EPSG:28992";
        Geometry geometry = wktReader.read(test);
        geometry.setSRID(28992);

        Geometry geometryExtent = MapRenderer.getGeometryExtent(geometry, localSrs, false);
        assertEquals(geometry.getEnvelope(), geometryExtent);
    }

    @Test
    public void extentGlobal3857Projection() throws Exception {
        String test = "POLYGON ((159.70909090909092 -85.06, 135 -76, 161 -76, 153 -81, 161.12 -85.06, 159.70909090909092 -85.06))";
        String globalSrs = "EPSG:3857";
        Geometry geometry = wktReader.read(test);
        geometry.setSRID(3857);

        // For global projections
        Geometry geometryExtent = MapRenderer.getGeometryExtent(geometry, globalSrs, true);
        assertEquals(geometry, geometryExtent);

        geometryExtent = MapRenderer.getGeometryExtent(geometry, globalSrs, false);
        assertEquals(geometry, geometryExtent);
    }

    @Test
    public void extentGlobal4326Projection() throws Exception {
        String test = "POLYGON ((165 -87, 135 -76, 161 -76, 153 -81, 165 -87))";
        String globalSrs = "EPSG:4326";
        Geometry geometry = wktReader.read(test);
        geometry.setSRID(4326);

        Geometry geometryExtent = MapRenderer.getGeometryExtent(geometry, globalSrs, true);
        assertEquals(geometry, geometryExtent);

        geometryExtent = MapRenderer.getGeometryExtent(geometry, globalSrs, false);
        assertEquals(geometry, geometryExtent);
    }
}
