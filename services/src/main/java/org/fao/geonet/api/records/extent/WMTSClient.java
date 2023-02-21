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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.map.WMTSMapLayer;
import org.geotools.ows.wmts.model.TileMatrixSet;
import org.geotools.ows.wmts.model.WMTSLayer;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Simple high-level api class for using GeoTools to do the underlying WMTS work
 * + get the appropriate tiles
 * + convert to appropriate scale
 * + convert to exact bounds
 */
public class WMTSClient {

    WMTSLayer layer;
    TileMatrixSet tileMatrixSet;
    WebMapTileServer wmts;


    public WMTSClient(URL url, String layerName, String matrixSet) throws Exception {
        wmts = new WebMapTileServer(url);
        layer = wmts.getCapabilities().getLayer(layerName);
        if (layer == null) {
            throw new Exception("no such layer as:" + layerName);
        }


        tileMatrixSet = wmts.getCapabilities().getMatrixSet(matrixSet);
        if (tileMatrixSet == null) {
            throw new Exception("no such tileMatrixSet:" + matrixSet);
        }
    }

    public BufferedImage createImage(double xmin, double ymin, double xmax, double ymax,
                                     int imageWidth, int imageHeight) throws IOException {
        ReferencedEnvelope envelope =
            new ReferencedEnvelope(
                xmin, xmax, ymin, ymax,
                tileMatrixSet.getCoordinateReferenceSystem());
        return createImage(envelope, imageWidth, imageHeight);
    }

    public BufferedImage createImage(double xmin, double ymin, double xmax, double ymax,
                                     int imageWidth, int imageHeight,
                                     String crsText) throws Exception {
        ReferencedEnvelope envelope =
            new ReferencedEnvelope(
                xmin, xmax, ymin, ymax,
                CRS.decode(crsText));

        ReferencedEnvelope envelope2 = envelope.transform(tileMatrixSet.getCoordinateReferenceSystem(), false);

        return createImage(envelope2, imageWidth, imageHeight);
    }

    /**
     * We use the Geotools StreamingRenderer (backed by a WMTSMapLayer) to draw the tiles appropriately.
     *
     * @return
     * @throws IOException
     */
    public BufferedImage createImage(ReferencedEnvelope envelope, int imageWidth, int imageHeight) throws IOException {
        WMTSMapLayer mapLayer = new WMTSMapLayer(wmts, layer, tileMatrixSet.getCoordinateReferenceSystem());

        MapContent mapContent = null;
        try {
            mapContent = new MapContent();
            mapContent.addLayer(mapLayer);
            mapContent.setViewport(new MapViewport(envelope));

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
            Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);

            Graphics2D gr = image.createGraphics();
            gr.setPaint(Color.WHITE);
            gr.fill(imageBounds);

            GTRenderer renderer = new StreamingRenderer();
            renderer.setMapContent(mapContent);


            renderer.paint(gr, imageBounds, envelope);
            return image;
        } finally {
            if (mapContent != null)
                mapContent.dispose();
        }
    }
}
