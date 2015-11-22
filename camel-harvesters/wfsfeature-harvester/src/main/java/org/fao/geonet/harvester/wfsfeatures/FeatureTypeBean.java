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
