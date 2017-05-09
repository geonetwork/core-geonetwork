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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.apache.camel.Exchange;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.apache.log4j.Logger;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//import org.elasticsearch.common.xcontent.XContentBuilder;

//import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsWFSFeatureIndexer {
    public static final String MULTIVALUED_SUFFIX = "s";
    public static final String TREE_FIELD_SUFFIX = "_tree";
    public static final String FEATURE_FIELD_PREFIX = "ft_";

    @Autowired
    private EsClient client;

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
     * Define for each attribute type the index field suffix.
     */
    private static Map<String, String> XSDTYPES_TO_FIELDSUFFIX;


    Logger logger = Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    // TODO: Move attributeType / dynamic index field suffix to config
    // maybe make a bean taking care of this which could also do more
    // complex mapping like based on feature type column name defined suffix
    public static final String DEFAULT_FIELDSUFFIX = "_s";

    static {
        XSDTYPES_TO_FIELDSUFFIX = ImmutableMap.<String, String>builder()
                .put("integer", "_ti")
                .put("string", DEFAULT_FIELDSUFFIX)
                .put("double", "_d")
                .put("boolean", "_b")
                .put("date", "_dt")
                .put("dateTime", "_dt")
                .build();
    }



    private int featureCommitInterval = 100;

    public int getFeatureCommitInterval() {
        return featureCommitInterval;
    }

    public void setFeatureCommitInterval(int featureCommitInterval) {
        this.featureCommitInterval = featureCommitInterval;
    }


    /**
     * Delete all documents matching a WFS server and a specific
     * typename.
     *
     * @param exchange
     */
    public void deleteFeatures(Exchange exchange) {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");
        final String url = state.getParameters().getUrl();
        final String typeName = state.getParameters().getTypeName();

        deleteFeatures(url, typeName, logger, client);
    }

    public static void deleteFeatures(String url, String typeName, Logger logger,
                                      EsClient client) {
        logger.info(String.format(
                "Deleting features previously index from service '%s' and feature type '%s' in '%s'",
                url, typeName, client.getCollection()));

        try {
            ZonedDateTime startTime = ZonedDateTime.now();
            String msg = client.deleteByQuery(
                client.getCollection(),
                String.format(
                    "+featureTypeId:\\\"%s#%s\\\"", url, typeName)
            );
            logger.info(String.format(
                    "  Features deleted in %sms.",
                    Duration.between(startTime, ZonedDateTime.now()).toMillis()));

            startTime = ZonedDateTime.now();
            msg = client.deleteByQuery(
                client.getCollection(),
                String.format(
                    "+id:\\\"%s#%s\\\"", url, typeName)
            );
            logger.info(String.format(
                    "  Report deleted in %sms.",
                    Duration.between(startTime, ZonedDateTime.now()).toMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format(
                    "Error connecting to ES at '%s'. Error is %s.",
                    client.getCollection(), e.getMessage()));
        }
    }

    private void saveHarvesterReport(WFSHarvesterExchangeState state) {
        Map<String, Object> report = state.getHarvesterReport();
        Index index = new Index.Builder(report)
            .index(client.getCollection())
            .type(client.getCollection())
            .id(report.get("id").toString()).build();
        try {
            DocumentResult response = client.getClient().execute(index);
            logger.info(String.format(
                    "Report saved for %s. Error is '%s'.",
                    state.getParameters().getTypeName(),
                    response.getErrorMessage()));
        } catch (Exception e) {
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

        state.getHarvesterReport().put("id", url + "#" + typeName);
        state.getHarvesterReport().put("docType", "harvesterReport");

        final Map<String, String> tokenizedFields = state.getParameters().getTokenizedFields();
        final List<String> treeFields = state.getParameters().getTreeFields();
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
                boolean isTree = treeFields != null ? treeFields.contains(attributeName) : false;
                documentFields.put(attributeName, FEATURE_FIELD_PREFIX +
                               attributeName +
                               XSDTYPES_TO_FIELDSUFFIX.get(attributeType) +
                               (isTree ? TREE_FIELD_SUFFIX : "")
                );
            }
        }
        state.getHarvesterReport().put("ftColumns_s", Joiner.on("|").join(featureAttributes.keySet()));
        state.getHarvesterReport().put("docColumns_s", Joiner.on("|").join(documentFields.values()));
        if (state.getParameters().getMetadataUuid() != null) {
            state.getHarvesterReport().put("parent", state.getParameters().getMetadataUuid());
        }

        // In WFS1.0.0, coordinates is lat/lon whatever the longitudeFirst is.
        CoordinateReferenceSystem wgs84;

        if (state.getParameters().getVersion().equals("1.0.0")) {
            wgs84 = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("EPSG:4326");
//            Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
//            CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
//            wgs84 = factory.createCoordinateReferenceSystem("4326");
        } else {
            wgs84 = CRS.getAuthorityFactory(true)
                .createCoordinateReferenceSystem("urn:x-ogc:def:crs:EPSG::4326");
        }

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
            query.setCoordinateSystemReproject(wgs84);
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection =
                source.getFeatures(query);

            final FeatureIterator<SimpleFeature> features = featuresCollection.features();
            int numInBatch = 0, nbOfFeatures = 0;
            Map<String, String> docCollection = new HashMap<>();

            saveHarvesterReport(state);

            String titleExpression = state.getParameters().getTitleExpression();
            String defaultTitleAttribute = titleExpression == null ?
                WFSFeatureUtils.guessFeatureTitleAttribute(featureAttributes) : null;

            ZonedDateTime startIndexingTime = ZonedDateTime.now();
            ZonedDateTime startCommitTime = null;

            StringBuffer bulkRequest = new StringBuffer();
            ObjectMapper mapper = new ObjectMapper();
            boolean isPointOnly = true;

            while (features.hasNext()) {
                ObjectNode rootNode = mapper.createObjectNode();
//                XContentBuilder builder = jsonBuilder()
//                    .startObject();

                try {
                    SimpleFeature feature = features.next();
                    nbOfFeatures ++;

                    String id = url + "#" + typeName + "#" + feature.getID();
//                    builder.field("docType", "feature");
//                    builder.field("resourceType", "feature");
//                    builder.field("featureTypeId", url + "#" + typeName);
                    rootNode.put("docType", "feature");
                    rootNode.put("resourceType", "feature");
                    rootNode.put("featureTypeId", url + "#" + typeName);


                    if (titleExpression != null) {
//                        builder.field("resourceTitle",
//                            WFSFeatureUtils.buildFeatureTitle(feature, featureAttributes, titleExpression));
                        rootNode.put("resourceTitle",
                            WFSFeatureUtils.buildFeatureTitle(feature, featureAttributes, titleExpression));
                    }

                    if (state.getParameters().getMetadataUuid() != null) {
//                        builder.field("parent", state.getParameters().getMetadataUuid());
                        rootNode.put("parent", state.getParameters().getMetadataUuid());
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
                                String[] tokens = ((String) attributeValue).split(separator);
                                ArrayNode arrayNode = mapper.createArrayNode();
                                for (String token : tokens) {
                                    arrayNode.add(token.trim());
                                }
                                rootNode.putPOJO(documentFields.get(attributeName), arrayNode);
//                                builder.endArray();
                            } else {
                                if (documentFields.get(attributeName).equals("geom")) {
                                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                                    // Convert to JSON
                                    String gjson = new GeometryJSON().toString(
                                        geom
                                    );
                                    JsonNode obj = mapper.readTree(gjson.getBytes(StandardCharsets.UTF_8));
//                                    builder.rawField(
                                    rootNode.put(
                                        documentFields.get(attributeName),
                                        obj
                                    );

                                    // Index point geometry in location field
                                    // of type geo_point
                                    boolean isPoint = geom instanceof Point;
                                    if (isPoint) {
                                        Coordinate point = geom.getCoordinate();
                                        // Strin with format lat,lon
                                        rootNode.put(
                                            "location",
                                            point.y + "," + point.x
                                        );
                                    } else {
                                        isPointOnly = false;
                                    }
                                } else {
//                                    builder.field(
                                    rootNode.put(
                                        documentFields.get(attributeName),
                                        attributeValue.toString());
                                }
                            }


                            if (defaultTitleAttribute != null &&
                                defaultTitleAttribute.equals(attributeName)) {
//                                builder.field("resourceTitle", attributeValue);
                                rootNode.put("resourceTitle", attributeValue.toString());
                            }
                        }
                    }
//                    builder.endObject();

//                    docCollection.put(id, builder.string());
                    docCollection.put(id, mapper.writeValueAsString(rootNode));
                } catch (Exception ex) {
                    state.getHarvesterReport().put("error_ss", String.format(
                        "Error while creating document for %s feature %d. Exception is: %s",
                        typeName, nbOfFeatures, ex.getMessage()
                    ));
                    continue;
                }


                numInBatch++;
                if (numInBatch >= featureCommitInterval) {
                    startCommitTime = ZonedDateTime.now();
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "  %s - %d features to index.",
                                typeName, nbOfFeatures));
                    }
                    try {
                        client.bulkRequest(client.getCollection(),
                            docCollection);
                    } catch (Exception ex) {
                        state.getHarvesterReport().put("error_ss", String.format(
                            "Error while indexing %s block of documents [%d-%d]. Exception is: %s",
                            typeName, nbOfFeatures, nbOfFeatures + numInBatch, ex.getMessage()
                        ));
                    }
                    docCollection.clear();
                    numInBatch = 0;
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                            "  %s - %d features indexed in %dms.",
                            typeName, nbOfFeatures,
                                Duration.between(startCommitTime, ZonedDateTime.now()).toMillis()
                        ));
                    }
                }
            }
            if (docCollection.size() > 0) {
                startCommitTime = ZonedDateTime.now();
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(
                        "  %s - %d features to index.",
                        typeName, nbOfFeatures));
                }
                try {
                    client.bulkRequest(client.getCollection(),
                        docCollection);
                } catch (Exception ex) {
                    state.getHarvesterReport().put("error_ss", String.format(
                        "Error while indexing %s block of documents [%d-%d]. Exception is: %s",
                        typeName, nbOfFeatures, nbOfFeatures + numInBatch, ex.getMessage()
                    ));
                }
                logger.debug(String.format(
                    "  %s - %d features indexed in %dms.",
                    typeName, nbOfFeatures,
                    Duration.between(startCommitTime, ZonedDateTime.now()).toMillis()
                ));
            }
            logger.info(String.format(
                "Total number of %s features indexed is %d in %dms.",
                typeName, nbOfFeatures,
                Duration.between(startCommitTime, ZonedDateTime.now()).toMillis()
            ));
            state.getHarvesterReport().put("isPointOnly", isPointOnly);
            state.getHarvesterReport().put("status_s", "success");
            state.getHarvesterReport().put("totalRecords_i", nbOfFeatures);
            final DateTime dateTime = new DateTime(DateTimeZone.UTC);
            state.getHarvesterReport().put("endDate_dt",
                    ISODateTimeFormat.yearMonthDay().print(dateTime) + 'T' +
                        ISODateTimeFormat.timeNoMillis().print(dateTime).replace("Z", ""));
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
