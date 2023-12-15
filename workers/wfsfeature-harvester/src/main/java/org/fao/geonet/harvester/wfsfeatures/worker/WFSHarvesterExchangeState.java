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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.impl.WFSDataAccessFactory;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by fgravin on 11/5/15.
 */
public class WFSHarvesterExchangeState implements Serializable {
    public static final String MAPSERVER_STRATEGY = "mapserver";

    public static final String QGIS_STRATEGY = "qgis";
    public static final String INVESTIGATOR_STRATEGY = "investigator";
    private WFSHarvesterParameter parameters;
    private transient Logger logger = LogManager.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private transient Map<String, String> fields = new LinkedHashMap<>();
    private transient WFSDataStore wfsDatastore = null;
    private List<String> resolvedTypeNames = new ArrayList<>();

    private String strategyId = null;

    public String getStrategyId() {
        return strategyId;
    }

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

    public List<String> getResolvedTypeNames() {
        return resolvedTypeNames;
    }


    private void checkTaskParameters() {
        if (StringUtils.isEmpty(parameters.getUrl())) {
            String errorMsg = "Empty WFS server URL is not allowed.";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (StringUtils.isEmpty(parameters.getTypeName())) {
            String errorMsg = "Check configuration for WFS {}. Empty type name is not allowed.";
            logger.error(errorMsg, parameters.getUrl());
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Create a WFSDatastore for this featuretype and retrieve
     * all schema infos (attributes names and types).
     */
    public void initDataStore() throws Exception {
        // Used to manage QGIS-Server based WFS
        WFSDataStoreFactory factory = null;
        if (INVESTIGATOR_STRATEGY.equals(parameters.getStrategy())) {
            factory = new WFSDataStoreWithStrategyInvestigator();
            ((WFSDataStoreWithStrategyInvestigator) factory).init(
                parameters.getUrl(), parameters.getTypeName());
        } else {
            factory = new WFSDataStoreFactory();
        }

        Map<String, Object> m = new HashMap<>();

        try {
            String getCapUrl = OwsUtils.getGetCapabilitiesUrl(
                parameters.getUrl(), parameters.getVersion());
            logger.info("Connecting using GetCatapbilities URL '{}'.", getCapUrl);

            m.put(WFSDataAccessFactory.URL.key, getCapUrl);
            m.put(WFSDataAccessFactory.TIMEOUT.key, parameters.getTimeOut());
            m.put(WFSDataAccessFactory.TRY_GZIP.key, true);
            m.put(WFSDataAccessFactory.ENCODING.key, parameters.getEncoding());
            m.put(WFSDataAccessFactory.USEDEFAULTSRS.key, true);
            m.put(WFSDataAccessFactory.OUTPUTFORMAT.key, "GML3"); // seems to be mandatory with wfs 1.1.0 sources
            m.put(WFSDataAccessFactory.LENIENT.key, true);
            if(!INVESTIGATOR_STRATEGY.equals(parameters.getStrategy())
                && StringUtils.isNotEmpty(parameters.getStrategy())) {
                m.put(WFSDataAccessFactory.WFS_STRATEGY.key, parameters.getStrategy());
            }

            if (parameters.getMaxFeatures() != -1) {
                m.put(WFSDataAccessFactory.MAXFEATURES.key, parameters.getMaxFeatures());
            }

            wfsDatastore = factory.createDataStore(m);
            // Default to GeoTools auto mode for MapServer.
            if(factory instanceof WFSDataStoreWithStrategyInvestigator) {
                WFSClientWithStrategyInvestigator wfsClientWithStrategyInvestigator = (WFSClientWithStrategyInvestigator) wfsDatastore.getWfsClient();
                this.strategyId = wfsClientWithStrategyInvestigator.getStrategyId();
                if (MAPSERVER_STRATEGY.equals(wfsClientWithStrategyInvestigator.getStrategyId())) {
                    Map<String, Object> connectionParameters = new HashMap<>();
                    connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", parameters.getUrl());
                    wfsDatastore = (WFSDataStore) DataStoreFinder.getDataStore(connectionParameters);
                }
            }
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

        logger.info("Reading feature type '{}' schema structure.",
            parameters.getTypeName());
        String typeSeparator = ",";
        List<String> featureTypeList = Arrays.asList(parameters.getTypeName().split(typeSeparator));

        String[] datastoreTypeNames = wfsDatastore.getTypeNames();
        String datastoreTypeNamesList = Arrays.stream(datastoreTypeNames)
            .collect(Collectors.joining(", "));

        for (String type : featureTypeList) {
            SimpleFeatureType sft = null;
            String resolvedFeatureTypeName = null;
            try {
                sft = wfsDatastore.getSchema(type);
                resolvedFeatureTypeName = type;
            } catch (IOException e) {
                logger.info(String.format(
                    "Type '%s' not found in data store. Available types are %s. Trying to found a match ignoring namespace.",
                    parameters.getTypeName(),
                    datastoreTypeNamesList
                ));
                Optional<String> typeFound = Arrays.stream(datastoreTypeNames)
                    .filter(t -> t.endsWith(type)).findFirst();
                if (typeFound.isPresent()) {
                    resolvedFeatureTypeName = typeFound.get();
                    logger.info("Found a type '{}'.", resolvedFeatureTypeName);
                    sft = wfsDatastore.getSchema(resolvedFeatureTypeName);
                } else {
                    throw new NoSuchElementException(String.format(
                        "No type found for '%s' (with or without namespace match).",
                        parameters.getTypeName()
                    ));
                }
            }
            if (sft != null) {
                List<AttributeDescriptor> attributesDesc = sft.getAttributeDescriptors();

                for (AttributeDescriptor desc : attributesDesc) {
                    if (!fields.containsKey(desc.getName().getLocalPart())) {
                        fields.put(desc.getName().getLocalPart(),
                            OwsUtils.getTypeFromFeatureType(desc));
                    }
                }
            }
            resolvedTypeNames.add(resolvedFeatureTypeName);
        }
        logger.info("Successfully analyzed {} attributes in schema.", fields.size());
    }
}
