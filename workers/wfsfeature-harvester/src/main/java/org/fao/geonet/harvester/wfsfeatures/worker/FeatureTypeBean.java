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

import org.apache.camel.Exchange;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;

/**
 * Created by fgravin on 11/5/15.
 */

@Component
public class FeatureTypeBean {
    Logger logger = Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);

    /**
     * Create exchange states for this feature type.
     *
     * Load configuration from exchange properties.
     * Could be a {@link WFSHarvesterParameter} or url and typeName
     * exchange properties.
     *
     * @param exchange
     * @param connect   Init datastore ie. connect to WFS, retrieve schema
     */
    public void initialize(Exchange exchange, boolean connect) throws InvalidArgumentException {
        WFSHarvesterParameter configuration =
                (WFSHarvesterParameter) exchange.getProperty("configuration");
        if (configuration == null) {
            throw new InvalidArgumentException("Missing WFS harvester configuration.");
        }

        logger.info(
                String.format(
                        "Initializing harvester configuration for uuid '%s', url '%s', feature type '%s'. Exchange id is '%s'.",
                        configuration.getMetadataUuid(),
                        configuration.getUrl(),
                        configuration.getTypeName(),
                        exchange.getExchangeId()
                ));

        WFSHarvesterExchangeState config = new WFSHarvesterExchangeState(configuration);
        if (connect) {
            try {
                config.initDataStore();
            } catch (Exception e) {
                String errorMsg = String.format(
                        "Failed to connect to server '%s'. Error is %s",
                        configuration.getUrl(),
                        e.getMessage());
                logger.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        exchange.setProperty("featureTypeConfig", config);
    }
}
