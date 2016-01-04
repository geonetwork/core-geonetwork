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

import org.apache.log4j.Logger;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fgravin on 11/5/15.
 */
public class FeatureTypeConfig {
    Logger logger = Logger.getLogger(HarvesterRouteBuilder.LOGGER_NAME);

    // TODO: move to config
    public static final int wfsTimeOut = 60000;
    public static final int wfsMaxFeatures = 2000;
    public static final String wfsEncoding = "UTF-8";
    private String uuid;
    private String wfsUrl;
    private String featureType;

    private WFSDataStore wfsDatastore = null;
    private Map<String, String> fields = new LinkedHashMap<String, String>();

    public String getFeatureType() {
        return featureType;
    }

    public String getWfsUrl() {
        return wfsUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public WFSDataStore getWfsDatastore() {
        return wfsDatastore;
    }

    FeatureTypeConfig(String uuid, String wfsUrl, String featureType) {
        this.uuid = uuid;
        this.wfsUrl = wfsUrl;
        this.featureType = featureType;
    }

    /**
     * Create a WFSDatastore for this featuretype and retrieve
     * all schema infos (attributes names and types).
     */
    public void connectToWfsService() throws Exception {
        WFSDataStoreFactory factory = new WFSDataStoreFactory();
        Map m = new HashMap();

        // See http://docs.geotools.org/latest/userguide/library/referencing/order.html
        // TODO: Discuss
        System.setProperty("org.geotools.referencing.forceXY", "true");

        try {
            String getCapUrl = OwsUtils.getGetCapabilitiesUrl(wfsUrl);
            logger.info(String.format("Connecting using GetCatapbilities URL '%s'.", getCapUrl));

            m.put(WFSDataStoreFactory.URL.key, getCapUrl);
            m.put(WFSDataStoreFactory.TIMEOUT.key, wfsTimeOut);
            m.put(WFSDataStoreFactory.TRY_GZIP, true);
            m.put(WFSDataStoreFactory.MAXFEATURES.key, wfsMaxFeatures);
            m.put(WFSDataStoreFactory.ENCODING, wfsEncoding);

            wfsDatastore = factory.createDataStore(m);

            logger.info(String.format("Reading feature type '%s' schema structure.", featureType));
            SimpleFeatureType sft = wfsDatastore.getSchema(featureType);
            List<AttributeDescriptor> attributesDesc = sft.getAttributeDescriptors();

            for (AttributeDescriptor desc : attributesDesc) {
                fields.put(desc.getName().getLocalPart(), OwsUtils.getTypeFromFeatureType(desc));
            }

            logger.info(String.format("Successfully analyzed %d attributes in schema.", fields.size()));
        } catch (IOException e) {
            String errorMsg = String.format("Failed to create datastore from service using URL '%s'. Error is %s.", wfsUrl, e.getMessage());
            logger.error(errorMsg);
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Failed to GetCatapbilities from service using URL '%s'. Error is %s.", wfsUrl, e.getMessage());
            logger.error(errorMsg);
            throw e;
        }
    }
}
