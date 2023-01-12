/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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

import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

import java.awt.*;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GetMapBaseMapRendererTest {


    @Test
    public void canHandle() {
        GetMapBaseMapRenderer mapRenderer = new GetMapBaseMapRenderer();

        assertTrue(mapRenderer.canHandle("http://example.com"));
        assertTrue(mapRenderer.canHandle("https://example.com"));

        assertFalse(mapRenderer.canHandle("{}"));
    }

    @Test
    public void modifyURL() throws Exception {
        GetMapBaseMapRenderer mapRenderer = new GetMapBaseMapRenderer();
        mapRenderer.configure("https://ows.terrestris.de/osm/service?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.0&LAYERS=OSM-WMS&STYLES=default&SRS={srs}&BBOX={minx},{miny},{maxx},{maxy}&WIDTH={width}&HEIGHT={height}&FORMAT=image/png",
            new Envelope(1, 2, 3, 4),
            "EPSG:4326",
            new Dimension(100, 100),
            null);

        URL finalURL = mapRenderer.createURL();

        assertEquals(new URL("https://ows.terrestris.de/osm/service?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.0&LAYERS=OSM-WMS&STYLES=default&SRS=EPSG:4326&BBOX=1.0,3.0,2.0,4.0&WIDTH=100&HEIGHT=100&FORMAT=image/png"),
            finalURL);
    }
}
