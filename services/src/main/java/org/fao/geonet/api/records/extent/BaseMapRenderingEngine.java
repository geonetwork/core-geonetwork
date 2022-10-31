package org.fao.geonet.api.records.extent;

import jeeves.server.context.ServiceContext;
import org.locationtech.jts.geom.Envelope;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * interface for rendering base maps.
 */
public interface BaseMapRenderingEngine {

    /**
     * return true if this renderer can handle this configuration
     *
     * @param configString
     * @return
     */
    boolean canHandle(String configString);

    void configure(String configString,
                   Envelope bbox,
                   String srs,
                   Dimension imageDimensions,
                   ServiceContext context) throws Exception;

    BufferedImage render() throws Exception;

}
