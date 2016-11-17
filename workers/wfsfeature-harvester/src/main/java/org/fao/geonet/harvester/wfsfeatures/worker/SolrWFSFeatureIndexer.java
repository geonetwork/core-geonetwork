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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrWFSFeatureIndexer {
    public static final String MULTIVALUED_SUFFIX = "s";
    public static final String FEATURE_FIELD_PREFIX = "ft_";
    private SolrClient solr;


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
    public void initialize(
            Exchange exchange,
            boolean connect) throws InvalidArgumentException {
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

    /**
     * Define for each attribute type the Solr field suffix.
     */
    private static Map<String, String> XSDTYPES_TO_SOLRFIELDSUFFIX;


    Logger logger = Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    // TODO: Move attributeType / solr dynamic index field suffix to config
    // maybe make a bean taking care of this which could also do more
    // complex mapping like based on feature type column name defined suffix
    public static final String DEFAULT_SOLRFIELDSUFFIX = "_s";

    static {
        XSDTYPES_TO_SOLRFIELDSUFFIX = ImmutableMap.<String, String>builder()
                .put("integer", "_ti")
                .put("string", DEFAULT_SOLRFIELDSUFFIX)
                .put("double", "_d")
                .put("boolean", "_b")
                .put("date", "_dt")
                .put("dateTime", "_dt")
                .build();
    }

    private String solrCollectionUrl;

    public void setSolrCollectionUrl(String solrCollectionUrl) {
        this.solrCollectionUrl = solrCollectionUrl;
    }

    public String getSolrCollectionUrl() {
        return solrCollectionUrl;
    }


    private int solrCommitWithinMs = 10000;

    public int getSolrCommitWithinMs() {
        return solrCommitWithinMs;
    }

    public SolrWFSFeatureIndexer setSolrCommitWithinMs(int solrCommitWithinMs) {
        this.solrCommitWithinMs = solrCommitWithinMs;
        return this;
    }


    private int featureCommitInterval = 200;

    public int getFeatureCommitInterval() {
        return featureCommitInterval;
    }

    public void setFeatureCommitInterval(int featureCommitInterval) {
        this.featureCommitInterval = featureCommitInterval;
    }


    /**
     * Delete all Solr documents matching a WFS server and a specific
     * typename.
     *
     * @param exchange
     */
    public void deleteFeatures(Exchange exchange) {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");
        final String url = state.getParameters().getUrl();
        final String typeName = state.getParameters().getTypeName();

        logger.info(String.format(
                "Deleting features previously index from service '%s' and feature type '%s' in '%s'",
                url, typeName, solrCollectionUrl));

        solr = new HttpSolrClient(solrCollectionUrl);
        try {
            UpdateResponse response = solr.deleteByQuery(String.format(
                    "+featureTypeId:\"%s#%s\"", url, typeName)
            );
            logger.info(String.format(
                    "  Features deleted in %sms.",
                    response.getElapsedTime()));
            response = solr.deleteByQuery(String.format(
                    "+id:\"%s#%s\"", url, typeName)
            );
            logger.info(String.format(
                    "  Report deleted in %sms.",
                    response.getElapsedTime()));
            solr.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
            logger.error(String.format(
                    "Error connecting to Solr server at '%s'. Error is %s.",
                    solrCollectionUrl, e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(String.format(
                    "Error connecting to Solr server at '%s'. Error is %s.",
                    solrCollectionUrl, e.getMessage()));
        }
    }

    private void saveHarvesterReport(WFSHarvesterExchangeState state) {
        solr = new HttpSolrClient(solrCollectionUrl);
        SolrInputDocument harvestingTaskDocument = new SolrInputDocument();
        Iterator<String> fields = state.getHarvesterReport().keySet().iterator();
        while (fields.hasNext()) {
            String field = fields.next();
            harvestingTaskDocument.addField(
                    field,
                    state.getHarvesterReport().get(field));
        }
        try {
            UpdateResponse response = solr.add(harvestingTaskDocument);
            logger.info(String.format(
                    "Report saved in %sms.",
                    response.getElapsedTime()));
            solr.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Index all features found in a WFS server
     *
     * @param exchange
     */
    public void indexFeatures(Exchange exchange) throws Exception {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");

        final String url = state.getParameters().getUrl();
        final String typeName = state.getParameters().getTypeName();
        logger.info(String.format(
                "Indexing WFS features from service '%s' and feature type '%s'",
                url, typeName));

        WFSDataStore wfs = state.getWfsDatastore();
        // Feature attribute name and type
        Map<String, String> featureAttributes = state.getFields();
        // Feature attribute name and document field name
        Map<String, String> documentFields = new LinkedHashMap<String, String>();

        solr = new HttpSolrClient(solrCollectionUrl);

        state.getHarvesterReport().put("id", url + "#" + typeName);
        state.getHarvesterReport().put("docType", "harvesterReport");

        final Map<String, String> tokenizedFields = state.getParameters().getTokenize();
        final boolean hasTokenizedFields = tokenizedFields != null;

        for (String attributeName : featureAttributes.keySet()) {
            String attributeType = featureAttributes.get(attributeName);
            String separator = null;
            if (hasTokenizedFields) {
                separator = tokenizedFields.get(attributeName);
            }
            if (attributeType.equals("geometry")) {
                documentFields.put(attributeName, "geom");
            } else {
                boolean isTokenized = separator != null;
                documentFields.put(attributeName, FEATURE_FIELD_PREFIX +
                               attributeName +
                               XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType) +
                               (isTokenized ? MULTIVALUED_SUFFIX : "")
                );
            }
        }
        state.getHarvesterReport().put("ftColumns_s", Joiner.on("|").join(featureAttributes.keySet()));
        state.getHarvesterReport().put("docColumns_s", Joiner.on("|").join(documentFields.values()));
        if (state.getParameters().getMetadataUuid() != null) {
            state.getHarvesterReport().put("parent", state.getParameters().getMetadataUuid());
        }

        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");

        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> source = wfs.getFeatureSource(typeName);
            Extent crsExtent = wgs84.getDomainOfValidity();
            ReferencedEnvelope wgs84bbox = null;
            for (GeographicExtent element : crsExtent.getGeographicElements()) {
                if (element instanceof GeographicBoundingBox) {
                    GeographicBoundingBox bounds = (GeographicBoundingBox) element;
                    wgs84bbox = new ReferencedEnvelope(
                            bounds.getSouthBoundLatitude(),
                            bounds.getNorthBoundLatitude(),
                            bounds.getWestBoundLongitude(),
                            bounds.getEastBoundLongitude(),
                            wgs84
                    );
                }
            }


            // TODO: GeoServer WFS 1.0.0 in some case return
//            Feb 18, 2016 12:04:22 PM org.geotools.data.wfs.v1_0_0.NonStrictWFSStrategy createFeatureReaderGET
//            WARNING: java.io.IOException: org.xml.sax.SAXException: cannot merge two target namespaces. http://www.openplans.org/topp http://www.openplans.org/spearfish
            // Retrieve features in WGS 84
            Query query = new Query();
            query.setCoordinateSystem(wgs84);

            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = source.getFeatures(query);

            final FeatureIterator<SimpleFeature> features = featuresCollection.features();
            int numInBatch = 0, nbOfFeatures = 0;
            Collection<SolrInputDocument> docCollection = new ArrayList<SolrInputDocument>();

            saveHarvesterReport(state);

            String titleExpression = state.getParameters().getTitleExpression();
            String defaultTitleAttribute = titleExpression == null ?
                WFSFeatureUtils.guessFeatureTitleAttribute(featureAttributes) : null;

            ZonedDateTime startIndexingTime = ZonedDateTime.now();
            ZonedDateTime startCommitTime = null;

            while (features.hasNext()) {
                SolrInputDocument document = new SolrInputDocument();
                try {
                    SimpleFeature feature = features.next();
                    nbOfFeatures ++;

                    document.addField("id", feature.getID());
                    document.addField("docType", "feature");
                    document.addField("resourceType", "feature");
                    document.addField("featureTypeId", url + "#" + typeName);

                    if (titleExpression != null) {
                        document.addField("resourceTitle",
                                WFSFeatureUtils.buildFeatureTitle(feature, featureAttributes, titleExpression));
                    }

                    if (state.getParameters().getMetadataUuid() != null) {
                        document.addField("parent", state.getParameters().getMetadataUuid());
                    }

                    for (String attributeName : featureAttributes.keySet()) {
                        String attributeType = featureAttributes.get(attributeName);
                        Object attributeValue = feature.getAttribute(attributeName);

                        if (attributeValue != null) {
                            // Check if field has to be tokenized
                            String separator = null;
                            if (hasTokenizedFields) {
                                separator = tokenizedFields.get(attributeName);
                            }
                            boolean isTokenized = separator != null;
                            if (isTokenized){
                                StringTokenizer tokenizer =
                                        new StringTokenizer((String) attributeValue, separator);
                                while (tokenizer.hasMoreElements()) {
                                    String token = tokenizer.nextToken();
                                    document.addField(
                                        documentFields.get(attributeName),
                                        token.trim());
                                }
                            } else {
                                if (documentFields.get(attributeName).equals("geom")) {
                                    document.addField(
                                            documentFields.get(attributeName),
                                            attributeValue.toString());
                                } else {
                                    document.addField(
                                            documentFields.get(attributeName),
                                            attributeValue);
                                }
                            }


                            if (defaultTitleAttribute != null &&
                                defaultTitleAttribute.equals(attributeName)) {
                                document.addField("resourceTitle", attributeValue);
                            }
                        }
                    }
                } catch (Exception ex) {
                    state.getHarvesterReport().put("error_ss", String.format(
                            "Error while creating document for feature %d. Exception is: %s",
                            nbOfFeatures, ex.getMessage()
                    ));
                    continue;
                }


                docCollection.add(document);
                numInBatch++;
                if (numInBatch >= featureCommitInterval) {
                    startCommitTime = ZonedDateTime.now();
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "  %d features to index.",
                                nbOfFeatures));
                    }
                    try {
                        UpdateResponse response = solr.add(docCollection);
                        solr.commit();
                    } catch (Exception ex) {
                        state.getHarvesterReport().put("error_ss", String.format(
                                "Error while indexing block of documents [%d-%d]. Exception is: %s",
                                nbOfFeatures, nbOfFeatures + numInBatch, ex.getMessage()
                        ));
                    }
                    docCollection.clear();
                    numInBatch = 0;
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "  %d features indexed in %dms.",
                                nbOfFeatures,
                                Duration.between(startCommitTime, ZonedDateTime.now()).toMillis()
                        ));
                    }
                }
            }
            if (docCollection.size() > 0) {
                startCommitTime = ZonedDateTime.now();
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(
                            "  %d features to index.",
                            nbOfFeatures));
                }
                try {
                    UpdateResponse response = solr.add(docCollection);
                    solr.commit();
                } catch (Exception ex) {
                    state.getHarvesterReport().put("error_ss", String.format(
                            "Error while indexing block of documents [%d-%d]. Exception is: %s",
                            nbOfFeatures, nbOfFeatures + numInBatch, ex.getMessage()
                    ));
                }
                logger.debug(String.format(
                    "  %d features indexed in %dms.",
                    nbOfFeatures,
                    Duration.between(startCommitTime, ZonedDateTime.now()).toMillis()
                ));
            }
            logger.info(String.format(
                "Total number of features indexed is %d in %dms.",
                nbOfFeatures,
                Duration.between(startIndexingTime, ZonedDateTime.now()).toMillis()
            ));
            state.getHarvesterReport().put("status_s", "success");
            state.getHarvesterReport().put("totalRecords_i", nbOfFeatures);
            state.getHarvesterReport().put("endDate_dt",
                    ISODateTimeFormat.dateTime().print(new DateTime()));
        } catch (Exception e) {
            state.getHarvesterReport().put("status_s", "error");
            state.getHarvesterReport().put("error_ss", e.getMessage());
            logger.error(e.getMessage());
            throw e;
        } finally {
            saveHarvesterReport(state);
        }
    }
}
