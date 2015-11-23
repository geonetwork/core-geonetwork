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

package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.Exchange;
import org.apache.solr.common.StringUtils;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;

/**
 * Created by fgravin on 11/5/15.
 */

@Component
public class FeatureTypeBean {
    Logger logger = Logger.getLogger(HarvesterRouteBuilder.LOGGER_NAME);

    private void checkParameters(String wfsUrl, String featureTypeName) {
        logger.info("Checking parameters ...");
        if (StringUtils.isEmpty(wfsUrl)) {
            String errorMsg = "Empty WFS server URL is not allowed.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (StringUtils.isEmpty(featureTypeName)) {
            String errorMsg = "Empty WFS type name is not allowed.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        logger.info("Parameters are accepted.");
    }

    public void initialize(Exchange exchange, boolean connect) {
        String uuid = (String) exchange.getProperty("mduuid");
        String wfsUrl = (String) exchange.getProperty("wfsUrl");
        String featureType = (String) exchange.getProperty("featureType");

        logger.info(
                String.format("Initializing harvester configuration for uuid '%s', url '%s', feature type '%s'. Exchange id is '%s'.",
                        uuid, wfsUrl, featureType, exchange.getExchangeId()
                ));
        checkParameters(wfsUrl, featureType);

        FeatureTypeConfig config = new FeatureTypeConfig(uuid, wfsUrl, featureType);
        if (connect) {
            try {
                config.connectToWfsService();
            } catch (Exception e) {
                String errorMsg = String.format("Failed to connect to server '%s'. Error is %s",
                        wfsUrl, e.getMessage());
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        exchange.setProperty("featureTypeConfig", config);
    }
}
