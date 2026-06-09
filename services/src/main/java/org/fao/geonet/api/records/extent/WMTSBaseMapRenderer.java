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

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.locationtech.jts.geom.Envelope;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * This handles WMTS-configured basemaps.
 * <p>
 * The configuration should be a json string with the following attributes;
 * <p>
 * wmtsGetCapabilitiesURL - (required) URL to the GetCapabilities for the WMTS service
 * layerName - (required)  Name of the layer to use (in the WMTS GetCapabilities)
 * <p>
 * matrixSet - (optional, default is the same as SRID) Name of the WMTS GetCapabilities matrixSet to use (hardcoded for all requests)
 * SRID2MatrixSet - (optional, default is no translation)
 * flip4326 - (optional, default is NO)
 * <p>
 * Typically, you should just need to set the `wmtsGetCapabilitiesURL` and `layerName`.
 * In this case, it will use the WMTS matrixSet with the same name as the SRS.
 * <p>
 * If you want to ALWAYS (!!) use a specific matrixSet, you can use the `matrixSet` parameter.
 * <p>
 * If the WMTS doesn't use the SRS as the name of the matrixSets, you can use the SRID2MatrixSet to convert the SRID
 * to a matrixSet.
 * <p>
 * Most WMTS systems will use YX ordering for EPSG:4326.  If you are supplying coordinates in XY ordering, you will
 * likely need to set flip4326 to true.
 * <p>
 * EXAMPLE
 * <p>
 * {
 * "wmtsGetCapabilitiesURL":"URL",
 * "layerName":"LAYERNAME",
 * "SRID2MatrixSet": {
 * "EPSG:900913":"EPSG:3857"
 * },
 * "flip4326": true
 * }
 * <p>
 * A request for SRID EPSG:1234 will use the "EPSG:1234" matrixSet.
 * A request for SRID EPSG:900913 will use the "EPSG:3857" matrixSet (SRID2MatrixSet).
 * If the request is for EPSG:4326, the request bbox (AOI) will be flipped from XY to YX.
 */
public class WMTSBaseMapRenderer implements BaseMapRenderingEngine {

    public static final String JSON_CapabilitiesURLTag= "wmtsGetCapabilitiesURL";
    public static final String JSON_layerNameTagTag= "layerName";
    public static final String JSON_matrixSetTag= "matrixSet";
    public static final String JSON_SRIDConvertTag= "SRID2MatrixSet";
    public static final String JSON_flip4326Tag= "flip4326";


    Envelope bbox;
    String srs;
    Dimension imageDimensions;
    ServiceContext context;

    URL getCapabilitiesURL;
    String layerName;

    Map<String, String> SRID2MatrixSet = new HashMap<>();
    boolean flip4326 = false;

    String urn4326 = "urn:ogc:def:crs:EPSG::4326"; // YX definition
    /**
     * null (not in config) -> matrix set is the same as srs
     * otherwise, use this matrix set.
     */
    String matrixSet; //null -> use srs

    public WMTSBaseMapRenderer() {

    }

    @Override
    public boolean canHandle(String configString) {
        return configString.startsWith("{");
    }

    public void configure(String configJsonString,
                          Envelope bbox,
                          String srs,
                          Dimension imageDimensions,
                          ServiceContext context) throws Exception {

        if (configJsonString == null) {
            throw new Exception("GetMapBaseMapRenderer: null json/configuration");
        }
        if (bbox == null) {
            throw new Exception("GetMapBaseMapRenderer: null ubboxrl");
        }
        if (srs == null) {
            throw new Exception("GetMapBaseMapRenderer: null srs");
        }
        if (imageDimensions == null) {
            throw new Exception("GetMapBaseMapRenderer: null imageDimensions");
        }


        this.bbox = bbox;
        this.srs = srs;
        this.imageDimensions = imageDimensions;
        this.context = context;

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> configuration = mapper.readValue(configJsonString, Map.class);

        if (!configuration.containsKey(JSON_CapabilitiesURLTag)
            || (configuration.get(JSON_CapabilitiesURLTag) == null)
            || (!configuration.get(JSON_CapabilitiesURLTag).toString().toLowerCase().startsWith("http"))
        ) {
            throw new Exception("WMTSBaseMapRenderer: json config key=wmtsGetCapabilitiesURL should be start with http or https");
        }

        getCapabilitiesURL = new URL(configuration.get(JSON_CapabilitiesURLTag).toString());

        if (!configuration.containsKey(JSON_layerNameTagTag)
            || (configuration.get(JSON_layerNameTagTag) == null)
            || (configuration.get(JSON_layerNameTagTag).toString().trim().isEmpty())
        ) {
            throw new Exception("WMTSBaseMapRenderer: json config key=layerName must be present");
        }

        layerName = configuration.get(JSON_layerNameTagTag).toString().trim();

        if (configuration.containsKey(JSON_matrixSetTag)
            && (configuration.get(JSON_matrixSetTag) != null)
            && (!configuration.get(JSON_matrixSetTag).toString().trim().isEmpty())
        ) {
            this.matrixSet = configuration.get(JSON_matrixSetTag).toString().trim();
        } else {
            this.matrixSet = srs;
        }

        if ((configuration.get(JSON_SRIDConvertTag) != null) && (configuration.get(JSON_SRIDConvertTag) instanceof Map)) {
            this.SRID2MatrixSet = (Map) configuration.get(JSON_SRIDConvertTag);
            if (SRID2MatrixSet.containsKey(matrixSet)) {
                matrixSet = SRID2MatrixSet.get(matrixSet);
            }
        }

        if ((configuration.get(JSON_flip4326Tag) != null) && (configuration.get(JSON_flip4326Tag) instanceof Boolean)) {
            if ((Boolean) configuration.get(JSON_flip4326Tag) && (srs.equals("EPSG:4326"))) {
                // if we are flipping, then we should use the YX definition
                this.srs = urn4326;
                flip4326 = true;
                this.bbox = new Envelope(bbox.getMinY(), bbox.getMaxY(), bbox.getMinX(), bbox.getMaxX());
                // might have to also flip height/width
            }
        }
    }

    /**
     * uses the WMTSClient to create a background map image based on the configuration and request-specific details.
     *
     * @return
     * @throws Exception
     */
    public BufferedImage render() throws Exception {
        WMTSClient wmtsClient = new WMTSClient(getCapabilitiesURL, layerName, matrixSet);


        BufferedImage image = wmtsClient.createImage(
            bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY(),
            (int) imageDimensions.getWidth(), (int) imageDimensions.getHeight(),
            this.srs);

        return image;
    }

}
