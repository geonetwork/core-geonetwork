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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.locationtech.jts.geom.Envelope;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Root class that hides the difference between the various background images (i.e. WMT GetMap or WMTS GetTile).
 */
public class BaseMapRenderer {

    //TODO: make this spring injectable, but not necessary at the moment.
    //      either make these factories, or add complexity so they don't need state
    BaseMapRenderingEngine[] baseMapRenderingEngines = new BaseMapRenderingEngine[]{
        new GetMapBaseMapRenderer(),
        new WMTSBaseMapRenderer()
    };
    /**
     * either a URL for a WMS (cf GetMapBaseMapRenderer)
     * or a JSON config for a WMTS (cf WMTSBaseMapRenderer)
     */
    String configuration;
    // area of interest
    Envelope bbox;
    //SRS of the request (ie EPSG:3857)
    String srs;
    //size of the image
    Dimension imageDimensions;
    //context of the web app (used for http proxy)
    ServiceContext context;


    public BaseMapRenderer(String configuration) {
        this.configuration = configuration;
    }

    /**
     * fluent api for setting the bbox of the AOI
     *
     * @param bbox
     * @return
     */
    public BaseMapRenderer bbox(Envelope bbox) {
        this.bbox = bbox;
        return this;
    }

    /**
     * fluent api for setting the size of the image
     *
     * @param imageDimensions
     * @return
     */
    public BaseMapRenderer imageDimensions(Dimension imageDimensions) {
        this.imageDimensions = imageDimensions;
        return this;
    }

    /**
     * fluent api for setting the SRS to use in the requests
     *
     * @param srs
     * @return
     */
    public BaseMapRenderer srs(String srs) {
        this.srs = srs;
        return this;
    }

    /**
     * fluent api for setting the web servicecontext
     *
     * @param context
     * @return
     */
    public BaseMapRenderer context(ServiceContext context) {
        this.context = context;
        return this;
    }

    /**
     * renders the background image (will retrieve from the internet).
     * Hands off the work to either the GetMapBaseMapRenderer or WMTSBaseMapRenderer
     *
     * @return
     * @throws Exception
     */
    public BufferedImage render() throws Exception {
        try {
            for (BaseMapRenderingEngine engine : baseMapRenderingEngines) {
                if (engine.canHandle(configuration)) {
                    engine.configure(configuration, bbox, srs, imageDimensions, context);
                    return engine.render();
                }
            }
            throw new Exception("didn't understand configuration (BaseMapRenderer) - " + configuration);
        } catch (Exception e) {
            Log.debug(Geonet.SPATIAL, "error occurred during BaseMapRender - " + e + " (IGNORED)");
            return new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_ARGB);
        }
    }
}
