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
import org.apache.commons.io.IOUtils;
import org.fao.geonet.lib.Lib;
import org.locationtech.jts.geom.Envelope;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * This handles rendering a BaseMap based on a pre-configured WMS request.
 * The configuration URL will have the following placeholders replaced with actual values;
 * <p>
 * {minx} and {MINX}
 * {miny} and {MINY}
 * {maxx} and {MAXX}
 * {maxy} and {MAXY}
 * {srs}  and {SRS}
 * {width} and {WIDTH}
 * {height} and {HEIGHT}
 * <p>
 * with the necessary values.
 * <p>
 * For example, the URL might look like this;
 * https://ows.terrestris.de/osm/service?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.0&LAYERS=OSM-WMS&STYLES=default&SRS={srs}&BBOX={minx},{miny},{maxx},{maxy}&WIDTH={width}&HEIGHT={height}&FORMAT=image/png
 */
public class GetMapBaseMapRenderer implements BaseMapRenderingEngine {

    String url;
    Envelope bbox;
    String srs;
    Dimension imageDimensions;
    ServiceContext context;

    public GetMapBaseMapRenderer() {

    }

    @Override
    public boolean canHandle(String configString) {
        return configString.toLowerCase().startsWith("http");
    }

    public void configure(String configuration,
                          Envelope bbox,
                          String srs,
                          Dimension imageDimensions,
                          ServiceContext context) throws Exception {

        if (configuration == null)
            throw new Exception("GetMapBaseMapRenderer: null url/configuration");
        if (bbox == null)
            throw new Exception("GetMapBaseMapRenderer: null ubboxrl");
        if (srs == null)
            throw new Exception("GetMapBaseMapRenderer: null srs");
        if (imageDimensions == null)
            throw new Exception("GetMapBaseMapRenderer: null imageDimensions");


        this.url = configuration;
        this.bbox = bbox;
        this.srs = srs;
        this.imageDimensions = imageDimensions;
        this.context = context;
    }

    /**
     * creates a BufferedImage by downloading the (modified) URL
     *
     * @return
     * @throws Exception
     */
    public BufferedImage render() throws Exception {
        URL url = createURL();
        InputStream in = null;
        BufferedImage image;
        try {
            // Setup the proxy for the request if required
            URLConnection conn = Lib.net.setupProxy(context, url);
            in = conn.getInputStream();
            BufferedImage original = ImageIO.read(in);
            image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(original, 0, 0, null);
            return image;
        } finally {
            if (in != null) {
                IOUtils.closeQuietly(in);
            }
        }
    }

    /**
     * replaces the URL placeholders with real values
     *
     * @return
     * @throws MalformedURLException
     */
    URL createURL() throws MalformedURLException {
        String _url = url;
        _url = _url.replace("{minx}", Double.toString(bbox.getMinX()))
            .replace("{maxx}", Double.toString(bbox.getMaxX()))
            .replace("{miny}", Double.toString(bbox.getMinY()))
            .replace("{maxy}", Double.toString(bbox.getMaxY()))
            .replace("{srs}", srs)
            .replace("{width}", Integer.toString(imageDimensions.width))
            .replace("{height}", Integer.toString(imageDimensions.height))
            .replace("{MINX}", Double.toString(bbox.getMinX()))
            .replace("{MAXX}", Double.toString(bbox.getMaxX()))
            .replace("{MINY}", Double.toString(bbox.getMinY()))
            .replace("{MAXY}", Double.toString(bbox.getMaxY()))
            .replace("{SRS}", srs)
            .replace("{WIDTH}", Integer.toString(imageDimensions.width))
            .replace("{HEIGHT}", Integer.toString(imageDimensions.height));
        return new URL(_url);
    }


}
