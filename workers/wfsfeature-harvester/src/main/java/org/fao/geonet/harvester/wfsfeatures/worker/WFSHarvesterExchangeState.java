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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fgravin on 11/5/15.
 */
public class WFSHarvesterExchangeState implements Serializable {
    private WFSHarvesterParameter parameters;
    private transient Logger logger = Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private transient Map<String, String> fields = new LinkedHashMap<String, String>();
    private transient WFSDataStore wfsDatastore = null;
    private String resolvedTypeName = null;

    public WFSHarvesterParameter getParameters() {
        return parameters;
    }

    public WFSHarvesterExchangeState(WFSHarvesterParameter parameters) {
        this.parameters = parameters;
        checkTaskParameters();
    }
    public void setParameters(WFSHarvesterParameter parameters) {
        this.parameters = parameters;
    }


    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setWfsDatastore(WFSDataStore wfsDatastore) {
        this.wfsDatastore = wfsDatastore;
    }

    public WFSDataStore getWfsDatastore() {
        return wfsDatastore;
    }

    public String getResolvedTypeName() {
        return resolvedTypeName;
    }


    private void checkTaskParameters() {
        logger.info("Checking parameters ...");
        if (StringUtils.isEmpty(parameters.getUrl())) {
            String errorMsg = "Empty WFS server URL is not allowed.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (StringUtils.isEmpty(parameters.getTypeName())) {
            String errorMsg = "Empty WFS type name is not allowed.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        logger.info("Parameters are accepted.");
    }

    /**
     * Create a WFSDatastore for this featuretype and retrieve
     * all schema infos (attributes names and types).
     */
    public void initDataStore() throws Exception {
        // Used to manage QGIS-Server based WFS
        WFSDataStoreFactory factory = null;
        if ("investigator".equals(parameters.getStrategy())) {
            factory = new WFSDataStoreWithStrategyInvestigator();
            ((WFSDataStoreWithStrategyInvestigator) factory).init(
                parameters.getUrl(), parameters.getTypeName());
        } else {
            factory = new WFSDataStoreFactory();
        }

        Map m = new HashMap();

        try {
            String getCapUrl = OwsUtils.getGetCapabilitiesUrl(
                    parameters.getUrl(), parameters.getVersion());
            logger.info(String.format(
                    "Connecting using GetCatapbilities URL '%s'.",
                    getCapUrl));

            m.put(WFSDataStoreFactory.URL.key, getCapUrl);
            m.put(WFSDataStoreFactory.TIMEOUT.key, parameters.getTimeOut());
            m.put(WFSDataStoreFactory.TRY_GZIP.key, true);
            m.put(WFSDataStoreFactory.ENCODING.key, parameters.getEncoding());
            m.put(WFSDataStoreFactory.USEDEFAULTSRS.key, true);
            m.put(WFSDataStoreFactory.OUTPUTFORMAT.key, "GML3"); // seems to be mandatory with wfs 1.1.0 sources
            m.put(WFSDataStoreFactory.LENIENT.key, true);
            if(!"investigator".equals(parameters.getStrategy())
                && StringUtils.isNotEmpty(parameters.getStrategy())) {
                m.put(WFSDataStoreFactory.WFS_STRATEGY.key, parameters.getStrategy());
            }

            if (parameters.getMaxFeatures() != -1) {
                m.put(WFSDataStoreFactory.MAXFEATURES.key, parameters.getMaxFeatures());
            }

            wfsDatastore = factory.createDataStore(m);

            logger.info(String.format(
                    "Reading feature type '%s' schema structure.",
                    parameters.getTypeName()));
            SimpleFeatureType sft = null;
            try {
                sft = wfsDatastore.getSchema(parameters.getTypeName());
                resolvedTypeName = parameters.getTypeName();
            } catch (IOException e) {
                String[] typeNames = wfsDatastore.getTypeNames();
                String typeNamesList = Arrays.stream(typeNames).collect(Collectors.joining(", "));
                logger.info(String.format(
                    "Type '%s' not found in data store. Available types are %s. Trying to found a match ignoring namespace.",
                    parameters.getTypeName(),
                    typeNamesList
                   ));
                Optional<String> typeFound = Arrays.stream(typeNames)
                    .filter(t -> t.endsWith(parameters.getTypeName())).findFirst();
                if (typeFound.isPresent()) {
                    resolvedTypeName = typeFound.get();
                    logger.info(String.format(
                        "Found a type '%s'.",
                        resolvedTypeName
                    ));
                    sft = wfsDatastore.getSchema(resolvedTypeName);
                } else {
                    throw new NoSuchElementException(String.format(
                        "No type found for '%s' (with or without namespace match).",
                        parameters.getTypeName()
                    ));
                }
            }

            List<AttributeDescriptor> attributesDesc = sft.getAttributeDescriptors();

            for (AttributeDescriptor desc : attributesDesc) {
                fields.put(desc.getName().getLocalPart(), OwsUtils.getTypeFromFeatureType(desc));
            }

            logger.info(String.format(
                    "Successfully analyzed %d attributes in schema.", fields.size()));
        } catch (IOException e) {
            String errorMsg = String.format(
                    "Failed to create datastore from service using URL '%s'. Error is %s.", parameters.getUrl(), e.getMessage());
            logger.error(errorMsg);
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to GetCapabilities from service using URL '%s'. Error is %s.",
                    parameters.getUrl(), e.getMessage());
            logger.error(errorMsg);
            throw e;
        }
    }
}
