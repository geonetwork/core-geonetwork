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

public class WMTSBaseMapRendererTest {

    String json = "{\n" +
        "                     \"wmtsGetCapabilitiesURL\":\"HTTP://example.com\",\n" +
        "                     \"layerName\":\"standard\",\n" +
        "                     \"SRID2MatrixSet\": {\n" +
        "                          \"EPSG:900913\":\"EPSG:3857\"\n" +
        "                     },\n" +
        "                     \"flip4326\": true\n" +
        "                   }";
    String jsonNoFlip = "{\n" +
        "                     \"wmtsGetCapabilitiesURL\":\"HTTPS://example.com\",\n" +
        "                     \"layerName\":\"standard\",\n" +
        "                     \"SRID2MatrixSet\": {\n" +
        "                          \"EPSG:900913\":\"EPSG:3857\"\n" +
        "                     },\n" +
        "                     \"flip4326\": false\n" +
        "                   }";

    @Test
    public void canHandle() {
        WMTSBaseMapRenderer mapRenderer = new WMTSBaseMapRenderer();

        assertFalse(mapRenderer.canHandle("http://example.com"));
        assertFalse(mapRenderer.canHandle("https://example.com"));

        assertTrue(mapRenderer.canHandle("{}"));
    }

    @Test
    public void parseSimple() throws Exception {
        WMTSBaseMapRenderer mapRenderer = new WMTSBaseMapRenderer();

        mapRenderer.configure(json, new Envelope(1, 2, 3, 4), "EPSG:3857", new Dimension(100, 100), null);

        assertEquals(new URL("HTTP://example.com"), mapRenderer.getCapabilitiesURL);
        assertEquals("standard", mapRenderer.layerName);

        assertEquals("EPSG:3857", mapRenderer.srs);
        assertEquals(new Dimension(100, 100), mapRenderer.imageDimensions);
        assertEquals(new Envelope(1, 2, 3, 4), mapRenderer.bbox);

    }

    @Test
    public void parseTranslate() throws Exception {
        WMTSBaseMapRenderer mapRenderer = new WMTSBaseMapRenderer();

        mapRenderer.configure(json, new Envelope(1, 2, 3, 4), "EPSG:900913", new Dimension(100, 100), null);

        // translates "EPSG:900913" to "EPSG:3857"
        assertEquals("EPSG:3857", mapRenderer.matrixSet);
    }


    @Test
    public void parseNoFlip4326() throws Exception {
        WMTSBaseMapRenderer mapRenderer = new WMTSBaseMapRenderer();

        mapRenderer.configure(jsonNoFlip, new Envelope(1, 2, 3, 4), "EPSG:4326", new Dimension(100, 100), null);

        assertEquals(false, mapRenderer.flip4326);
        assertEquals("EPSG:4326", mapRenderer.srs);
        assertEquals(new Envelope(1, 2, 3, 4), mapRenderer.bbox);
    }

    @Test
    public void parseFlip4326() throws Exception {
        WMTSBaseMapRenderer mapRenderer = new WMTSBaseMapRenderer();

        mapRenderer.configure(json, new Envelope(1, 2, 3, 4), "EPSG:4326", new Dimension(100, 100), null);

        assertEquals(true, mapRenderer.flip4326);
        assertEquals("urn:ogc:def:crs:EPSG::4326", mapRenderer.srs); // this will change
        assertEquals(new Envelope(3, 4, 1, 2), mapRenderer.bbox);
    }

}
