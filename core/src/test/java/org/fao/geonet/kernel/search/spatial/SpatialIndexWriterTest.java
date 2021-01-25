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

package org.fao.geonet.kernel.search.spatial;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.util.factory.Hints;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKTReader;

import static org.junit.Assert.*;

public class SpatialIndexWriterTest {
    protected GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(new Hints(Hints.JTS_SRID, 4326));

    @Test
    public void point() throws Exception {
        checkBounds("POINT(0 0)");
    }
    @Test
    public void points() throws Exception {
        checkBounds("MULTIPOINT(0 0,1 0,1 1,0 1,0 0)");
    }
    @Test
    public void line() throws Exception {
        checkBounds("LINESTRING(0 0,1 1)");
    }
    @Test
    public void ring() throws Exception {
        checkBounds("LINEARRING(0 0,1 0,1 1,0 1,0 0)");
    }
    @Test
    public void lines() throws Exception {
        checkBounds("MULTILINESTRING ((10 10, 20 20, 10 40),(40 40, 30 30, 40 20, 30 10))");
    }
    @Test
    public void polygon() throws Exception {
        checkBounds("POLYGON((0 0,1 0,1 1,0 1,0 0))");
        checkBounds("POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
    }
    @Test
    public void polygons() throws Exception {
        checkBounds("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
    }
    @Test
    public void collection() throws Exception {
        checkBounds( "GEOMETRYCOLLECTION (POINT (40 10),LINESTRING (10 10, 20 20, 10 40),POLYGON ((40 40, 20 45, 45 30, 40 40)))");
    }

    protected void checkBounds(String wkt) throws Exception {
        String message = wkt.substring(0, wkt.indexOf('('));
        WKTReader reader = new WKTReader(factory);

        Geometry geometry = reader.read(wkt);

        MultiPolygon bounds = SpatialIndexWriter.toMultiPolygon(geometry);
        checkBounds(message, geometry, bounds);
    }

    protected void checkBounds(String message, Geometry geometry, MultiPolygon bounds){
        if(geometry ==null && bounds ==null) {
            return;
        }
        assertNotNull(message+": geom", geometry );
        assertNotNull( message+": bounds", bounds );
        if( geometry instanceof GeometryCollection){
            GeometryCollection collection = (GeometryCollection) geometry;
            for( int i=0; i < collection.getNumGeometries(); i++){
                assertTrue("coveredBy "+i, collection.getGeometryN(i).coveredBy(bounds));
            }
        }
        else {
            assertTrue("coveredBy", geometry.coveredBy(bounds));
        }
        assertEquals( "srid",geometry.getSRID(), bounds.getSRID() );
        assertEquals( "data", geometry.getUserData(), bounds.getUserData() );
    }
}
