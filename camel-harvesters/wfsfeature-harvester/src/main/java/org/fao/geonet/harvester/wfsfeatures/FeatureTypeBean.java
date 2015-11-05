package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.Exchange;
import org.geotools.data.DataStore;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fgravin on 11/5/15.
 */

@Component
public class FeatureTypeBean {

    public void initialize(Exchange exchange) {
        String uuid = (String)exchange.getProperty("mduuid");
        String wfsUrl = (String)exchange.getProperty("wfsUrl");
        String featureType = (String)exchange.getProperty("featureType");

        FeatureTypeConfig config = new FeatureTypeConfig(uuid, wfsUrl, featureType);
        config.connectToWfsService();

        exchange.setProperty("featureTypeConfig", config);

    }
}
