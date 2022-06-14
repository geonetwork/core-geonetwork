/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.harvester.wfsfeatures.worker;

import org.apache.log4j.Logger;
import org.geotools.http.HTTPClient;
import org.geotools.http.HTTPResponse;
import org.geotools.data.wfs.internal.WFSClient;
import org.geotools.data.wfs.internal.WFSConfig;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.geotools.data.wfs.internal.WFSStrategy;
import org.geotools.data.wfs.internal.v1_x.MapServerWFSStrategy;
import org.geotools.ows.ServiceException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * WFSClient which use the DescribeFeatureType request to determine
 * which WFSStrategy to user.
 */
public class WFSClientWithStrategyInvestigator extends WFSClient {
    private String describeFeatureTypeUrl;
    private transient Logger logger = Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);

    public WFSClientWithStrategyInvestigator(URL capabilitiesURL, HTTPClient httpClient, WFSConfig config, String describeFeatureTypeUrl) throws IOException, ServiceException {
        this(capabilitiesURL, httpClient, config, (WFSGetCapabilities) null, describeFeatureTypeUrl);
    }

    public WFSClientWithStrategyInvestigator(URL capabilitiesURL, HTTPClient httpClient, WFSConfig config, WFSGetCapabilities capabilities, String describeFeatureTypeUrl) throws IOException, ServiceException {
        super(capabilitiesURL, httpClient, config, capabilities);
        this.describeFeatureTypeUrl = describeFeatureTypeUrl;
        logger.debug(String.format(
            "WFS client default strategy is %s", getStrategy()));
        WFSStrategy targetNsBasedStrategy = this.determineCorrectStrategy(httpClient);
        if (targetNsBasedStrategy != null) {
            logger.debug(String.format(
                "Overriding WFS client strategy with GFI target namespace strategy: %s", targetNsBasedStrategy.getClass().getName()));
            super.specification = targetNsBasedStrategy;
            ((WFSStrategy) specification).setCapabilities(super.capabilities);
        }
    }


    private WFSStrategy determineCorrectStrategy(HTTPClient httpClient) {
        WFSStrategy strategy = null;

        try {
            logger.debug(String.format("Determining strategy based on %s", describeFeatureTypeUrl));
            HTTPResponse httpResponse = httpClient.get(new URL(describeFeatureTypeUrl));
            InputStream inputStream = httpResponse.getResponseStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int readCount;
            while ((readCount = inputStream.read(buff)) != -1) {
                out.write(buff, 0, readCount);
            }
            String responsePayload = out.toString("UTF-8");
            if (responsePayload.contains("targetNamespace=\"http://www.qgis.org/gml\"")) {
                strategy = new QgisStrategy();
            } else if (responsePayload.contains("targetNamespace=\"http://mapserver.gis.umn.edu/mapserver\"")) {
                strategy = new MapServerWFSStrategy(capabilities.getRawDocument());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strategy;
    }
}
