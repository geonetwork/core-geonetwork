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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.apache.camel.Exchange;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.fao.geonet.es.EsClient;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// TODO: GeoServer WFS 1.0.0 in some case return
// Feb 18, 2016 12:04:22 PM org.geotools.data.wfs.v1_0_0.NonStrictWFSStrategy createFeatureReaderGET
// WARNING: java.io.IOException: org.xml.sax.SAXException: cannot merge two target namespaces. http://www.openplans.org/topp http://www.openplans.org/spearfish


public class EsWFSFeatureIndexer {
    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_START_REGEX = "<!\\[CDATA\\[";
    public static final String CDATA_END = "]]>";
    private static Logger LOGGER =  LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    static {
        try {
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Value("${es.index.features}")
    private String index = "features";

    @Value("${es.index.features.type}")
    private String indexType = "features";


    @Value("${es.index.features.featureCommitInterval:300}")
    private int featureCommitInterval;

    public int getFeatureCommitInterval() {
        return featureCommitInterval;
    }

    public void setFeatureCommitInterval(int featureCommitInterval) {
        this.featureCommitInterval = featureCommitInterval;
    }


    @Value("${es.index.features.applyPrecisionModel:false}")
    private boolean applyPrecisionModel;

    public boolean isApplyPrecisionModel() {
        return applyPrecisionModel;
    }

    public void setApplyPrecisionModel(boolean applyPrecisionModel) {
        this.applyPrecisionModel = applyPrecisionModel;
    }


    @Value("${es.index.features.numberOfDecimals:7}")
    private int numberOfDecimals;

    public int getNumberOfDecimals() {
        return numberOfDecimals;
    }

    public void setNumberOfDecimals(int numberOfDecimals) {
        this.numberOfDecimals = numberOfDecimals;
    }

    @Autowired
    private EsClient client;

    public void setIndex(String index) {
        this.index = index;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    private ObjectMapper jacksonMapper = new ObjectMapper();

    private int nbOfFeatures;

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
        WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
        if (configuration == null) {
            throw new InvalidArgumentException("Missing WFS harvester configuration.");
        }

        LOGGER.info("Initializing harvester configuration for uuid '{}', url '{}', feature type '{}'. Exchange id is '{}'.", new Object[] {
            configuration.getMetadataUuid(),
            configuration.getUrl(),
            configuration.getTypeName(),
            exchange.getExchangeId()});

        WFSHarvesterExchangeState config = new WFSHarvesterExchangeState(configuration);
        if (connect) {
            try {
                config.initDataStore();
            } catch (Exception e) {
                String errorMsg = String.format("Failed to connect to server '%s'. Error is %s", configuration.getUrl(), e.getMessage());
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        exchange.setProperty("featureTypeConfig", config);
    }

    /**
     * Delete all documents matching a WFS server and a specific typename.
     *
     * @param exchange
     */
    public void deleteFeatures(Exchange exchange) {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");
        final String url = state.getParameters().getUrl();
        final String typeName = state.getParameters().getTypeName();

        deleteFeatures(url, typeName, client);
    }

    public void deleteFeatures(String url, String typeName, EsClient client) {
        LOGGER.info("Deleting features previously index from service '{}' and feature type '{}' in index '{}/{}'",
            new Object[]{url, typeName, index, indexType});
        try {
            long begin = System.currentTimeMillis();
            client.deleteByQuery(index, String.format("+featureTypeId:\\\"%s\\\"", getIdentifier(url, typeName)));
            LOGGER.info("  Features deleted in {} ms.", System.currentTimeMillis() - begin);

            begin = System.currentTimeMillis();
            client.deleteByQuery(index, String.format("+id:\\\"%s\\\"",
                getIdentifier(url, typeName)));
            LOGGER.info("  Report deleted in {} ms.", System.currentTimeMillis() - begin);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error( "Error connecting to ES at '{}'. Error is {}.", index, e.getMessage());
        }
    }

    interface TitleResolver {
        void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature);
    }

    public void indexFeatures(Exchange exchange) throws Exception {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");

        String url                              = state.getParameters().getUrl();
        String typeName                         = state.getParameters().getTypeName();
        Map<String, String> tokenizedFields     = state.getParameters().getTokenizedFields();
        WFSDataStore wfs                        = state.getWfsDatastore();
        Map<String, String> featureAttributes   = state.getFields();
        TitleResolver titleResolver = getTitleResolver(state);

        LOGGER.info("Indexing WFS features from service '{}' and feature type '{}'. Precision model applied: '{}', number of decimals: '{}'", url, typeName, applyPrecisionModel, numberOfDecimals);
        Report report= new Report(url, typeName);
        ObjectNode protoNode = createProtoNode(url, typeName);
        if (state.getParameters().getMetadataUuid() != null) {
            report.put("parent", state.getParameters().getMetadataUuid());
            protoNode.put("parent", state.getParameters().getMetadataUuid());
        }
        initFeatureAttributeToDocumentFieldNamesMapping(featureAttributes, state.getParameters().getTreeFields(), report);
        boolean initializeESReportSucceeded = report.saveHarvesterReport();
        if (!initializeESReportSucceeded) {
            LOGGER.error("couldn't initialize es report, don't even try to go further querying wfs.");
            throw new RuntimeException("couldn't initialize es report, don't even try to go further querying wfs.");
        }

        Query query = new Query();
        CoordinateReferenceSystem wgs84;
        if (wfs.getInfo().getVersion().equals("1.0.0")) {
            wgs84 = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("EPSG:4326");
        } else {
            wgs84 = CRS.getAuthorityFactory(true).createCoordinateReferenceSystem("urn:x-ogc:def:crs:EPSG::4326");
        }
        query.setCoordinateSystemReproject(wgs84);

        try {
            nbOfFeatures = 0;

            final Phaser phaser = new Phaser();
            BulkResutHandler brh = new AsyncBulkResutHandler(phaser, typeName, url, nbOfFeatures, report);

            long begin = System.currentTimeMillis();
            FeatureIterator<SimpleFeature> features = wfs.getFeatureSource(typeName).getFeatures(query).features();
            while (features.hasNext()) {

                try {
                    SimpleFeature feature = features.next();
                    ObjectNode rootNode = protoNode.deepCopy();
                    titleResolver.setTitle(rootNode, feature);

                    for (String attributeName : featureAttributes.keySet()) {
                        Object attributeValue = feature.getAttribute(attributeName);
                        if (attributeValue == null) {

                        } else if (tokenizedFields != null && tokenizedFields.get(attributeName) != null) {
                            String rawValue = (String) attributeValue;
                            String value = rawValue.startsWith(CDATA_START) ?
                                rawValue.replaceFirst(CDATA_START_REGEX, "").substring(0, rawValue.length() - CDATA_END.length() - CDATA_START.length()) :
                                rawValue;

                            String separator = tokenizedFields.get(attributeName);
                            String[] tokens = value.split(separator);
                            ArrayNode arrayNode = jacksonMapper.createArrayNode();
                            for (String token : tokens) {
                                arrayNode.add(token.trim());
                            }
                            rootNode.putPOJO(getDocumentFieldName(attributeName), arrayNode);
                        } else if (getDocumentFieldName(attributeName).equals("geom")) {
                            Geometry geom = (Geometry) feature.getDefaultGeometry();

                            if (applyPrecisionModel) {
                                PrecisionModel precisionModel = new PrecisionModel(Math.pow(10, numberOfDecimals - 1));
                                geom = GeometryPrecisionReducer.reduce(geom, precisionModel);
                                // numberOfDecimals is equal to
                                // precisionModel.getMaximumSignificantDigits()
                            }

                            // An issue here is that GeometryJSON conversion may over simplify
                            // the geometry by truncating coordinates based on numberOfDecimals
                            // which on default constructor is set to 4. This may lead to
                            // invalid geometry and Elasticsearch will fail parsing the GeoJSON
                            // with the following type of error:
                            // Caused by: org.locationtech.spatial4j.exception.InvalidShapeException:
                            // Provided shape has duplicate
                            // consecutive coordinates at: (-3.9997, 48.7463, NaN)
                            //
                            // To avoid this, it may be relevant to apply the reduction model
                            // preserving topology.
                            String gjson = new GeometryJSON(numberOfDecimals).toString(geom);

                            JsonNode jsonNode = jacksonMapper.readTree(gjson.getBytes(StandardCharsets.UTF_8));
                            rootNode.put(getDocumentFieldName(attributeName), jsonNode);

                            boolean isPoint = geom instanceof Point;
                            if (isPoint) {
                                Coordinate point = geom.getCoordinate();
                                rootNode.put("location", String.format("%s,%s", point.y , point.x));
                            } else {
                                report.setPointOnlyForGeomsFalse();
                            }

                            // Populate bbox coordinates to be able to compute
                            // global bbox of search results
                            final BoundingBox bbox = feature.getBounds();
                            rootNode.put("bbox_xmin", bbox.getMinX());
                            rootNode.put("bbox_ymin", bbox.getMinY());
                            rootNode.put("bbox_xmax", bbox.getMaxX());
                            rootNode.put("bbox_ymax", bbox.getMaxY());

                        } else {
                            String value = attributeValue.toString();
                            rootNode.put(getDocumentFieldName(attributeName),
                                value.startsWith(CDATA_START) ?
                                    value.replaceFirst(CDATA_START_REGEX, "").substring(0, value.length() - CDATA_END.length() - CDATA_START.length()) :
                                    value

                            );
                        }
                    }

                    nbOfFeatures ++;
                    brh.addAction(rootNode, feature);

                } catch (Exception ex) {
                    LOGGER.warn("Error while creating document for {} feature {}. Exception is: {}", new Object[] {
                        typeName, nbOfFeatures, ex.getMessage()});
                    report.put("error_ss", String.format(
                        "Error while creating document for %s feature %d. Exception is: %s",
                        typeName, nbOfFeatures, ex.getMessage()
                    ));
                }

                if (brh.getBulkSize() >= featureCommitInterval) {
                    brh.launchBulk(client);
                    brh = new AsyncBulkResutHandler(phaser, typeName, url, nbOfFeatures, report);
                }
            }

            if (brh.getBulkSize() > 0) {
                brh.launchBulk(client);
            }

            try {
                if (nbOfFeatures > 0) {
                    phaser.awaitAdvanceInterruptibly(0, 3, TimeUnit.HOURS);
                }
            } catch (TimeoutException e) {
                throw new Exception("Timeout when awaiting all bulks to be processed.");
            }
            LOGGER.info("Total number of {} features indexed is {} in {} ms.", new Object[]{
                typeName, nbOfFeatures,
                System.currentTimeMillis() - begin});
            report.success(nbOfFeatures);
        } catch (Exception e) {
            report.put("status_s", "error");
            report.put("error_ss", e.getMessage());
            LOGGER.error(e.getMessage());
            throw e;
        } finally {
            report.saveHarvesterReport();
        }
    }

    private TitleResolver getTitleResolver(WFSHarvesterExchangeState state) {
        TitleResolver titleResolver;
        String titleExpression = state.getParameters().getTitleExpression();
        String defaultTitleAttribute = titleExpression == null ? WFSFeatureUtils.guessFeatureTitleAttribute(state.getFields()) : null;
        if (titleExpression != null) {
            titleResolver = new TitleResolver() {
                @Override
                public void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature) {
                    objectNode.put("resourceTitle",
                        WFSFeatureUtils.buildFeatureTitle(simpleFeature, state.getFields(), titleExpression));
                }
            };
        } else if (defaultTitleAttribute !=null) {
            titleResolver = new TitleResolver() {
                @Override
                public void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature) {
                    objectNode.put("resourceTitle", simpleFeature.getAttribute(defaultTitleAttribute).toString());
                }
            };
        } else {
            titleResolver = new TitleResolver() {
                @Override
                public void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature) {

                }
            };
        }
        return titleResolver;
    }

    private ObjectNode createProtoNode(String url, String typeName) {
        ObjectNode protoNode = jacksonMapper.createObjectNode();
        protoNode.put("docType", "feature");
        protoNode.put("resourceType", "feature");
        protoNode.put("featureTypeId", getIdentifier(url, typeName));
        return protoNode;
    }

    class Report {
        private Map<String, Object> report = new HashMap<>();
        private String url;
        private String typeName;
        private boolean pointOnlyForGeoms;

        public Report(String url, String typeName) throws UnsupportedEncodingException {
            this.typeName = typeName;
            this.url = url;
            pointOnlyForGeoms = true;
            report.put("id", getIdentifier(url, typeName));
            report.put("docType", "harvesterReport");
        }

        public void put(String key, Object value) {
            report.put(key, value);
        }

        public void setPointOnlyForGeomsFalse() {
            this.pointOnlyForGeoms = false;
        }

        public void success(int nbOfFeatures) {
            report.put("status_s","success");
            report.put("totalRecords_i", nbOfFeatures);
            DateTime dateTime = new DateTime(DateTimeZone.UTC);
            report.put("endDate_dt", String.format("%sT%s",
                ISODateTimeFormat.yearMonthDay().print(dateTime),
                ISODateTimeFormat.timeNoMillis().print(dateTime).replace("Z","")));
            report.put("isPointOnly", pointOnlyForGeoms);

        }

        public boolean saveHarvesterReport() {
            Index search = new Index.Builder(report)
                .index(index)
                .type("_doc")
                .id(report.get("id").toString()).build();
            try {
                DocumentResult response = client.getClient().execute(search);
                if (response.getErrorMessage() != null) {
                    LOGGER.info("Failed to save report for {}. Error message when saving report was '{}'.",
                        typeName,
                        response.getErrorMessage());
                } else {
                    LOGGER.info("Report saved for service {} and typename {}. Report id is {}",
                        url, typeName, report.get("id"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private String getIdentifier(String url, String typeName) {
        try {
            return URLEncoder.encode(url + "#" + typeName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("  Can not build an URL encoded identifier from {}#{}. Exception is {}.",
                url, typeName, e.getMessage());
            return null;
        }
    }

    abstract class BulkResutHandler implements JestResultHandler<BulkResult> {

        protected Phaser phaser;
        protected String typeName;
        private String url;
        protected int firstFeatureIndex;
        private Report report;
        protected long begin;
        protected Bulk.Builder bulk;
        protected int bulkSize;

        public BulkResutHandler(Phaser phaser, String typeName, String url, int firstFeatureIndex, Report report) {
            this.phaser = phaser;
            this.typeName = typeName;
            this.url = url;
            this.firstFeatureIndex = firstFeatureIndex;
            this.report = report;
            this.bulk = new Bulk.Builder().defaultIndex(index);
            this.bulkSize = 0;
            LOGGER.debug("  {} - from {}, {} features to index, preparing bulk.", typeName, firstFeatureIndex, featureCommitInterval);
        }

        @Override
        public void completed(BulkResult bulkResult) {
            LOGGER.debug("  {} - from {}, {}/{} features, indexed in {} ms.", new Object[]{
                typeName, firstFeatureIndex, bulkSize, featureCommitInterval, System.currentTimeMillis() - begin});
            phaser.arriveAndDeregister();
        }

        @Override
        public void failed(Exception e) {
            this.report.put("error_ss", String.format(
                "Error while indexing %s block of documents [%d-%d]. Exception is: %s",
                typeName, firstFeatureIndex, firstFeatureIndex + featureCommitInterval, e.getMessage()
            ));
            LOGGER.error("  {} - from {}, {}/{} features, NOT indexed in {} ms. ({}).", new Object[]{
                typeName, firstFeatureIndex, bulkSize, featureCommitInterval, System.currentTimeMillis() - begin, e.getMessage()});
            phaser.arriveAndDeregister();
        }

        public int getBulkSize() {
            return bulkSize;
        }

        public void addAction(ObjectNode rootNode, SimpleFeature feature) throws JsonProcessingException {
            // generate a unique feature id when geotools gives us a placeholder one
            String featureId = feature.getID();
            if (featureId.toLowerCase().indexOf("placeholder") > -1) {
                featureId = "fid-" + nbOfFeatures;
            }

            String id = String.format("%s#%s#%s", url, typeName, featureId);
            bulk.addAction(new Index.Builder(jacksonMapper.writeValueAsString(rootNode)).id(id).build());
            bulkSize++;
        }

        protected void prepareLaunch() {
            phaser.register();
            this.begin = System.currentTimeMillis();
            LOGGER.debug("  {} - from {}, {}/{} features, launching bulk.", new Object[]{
                typeName, firstFeatureIndex, bulkSize, featureCommitInterval,});
        }
        abstract public void launchBulk(EsClient client);
    }

    // depending on situation, one can expect going up to 1.5 faster using an async result handler (e.g. hudge collection of points)
    class AsyncBulkResutHandler extends BulkResutHandler {
        public AsyncBulkResutHandler(Phaser phaser, String typeName, String url, int firstFeatureIndex, Report report) {
            super(phaser, typeName, url, firstFeatureIndex, report);
        }

        public void launchBulk(EsClient client) {
            prepareLaunch();
            client.bulkRequestAsync(this.bulk, this);
        }
    }

    class SyncBulkResutHandler extends BulkResutHandler {
        public SyncBulkResutHandler(Phaser phaser, String typeName, String url, int firstFeatureIndex, Report report) {
            super(phaser, typeName, url, firstFeatureIndex, report);
        }

        public void launchBulk(EsClient client) {
            try {
                prepareLaunch();
                BulkResult result = client.bulkRequestSync(this.bulk);
                if (result.isSucceeded()) {
                    this.completed(result);
                } else {
                    this.failed(new Exception(result.getErrorMessage()));
                }
            } catch (IOException e) {
                this.failed(e);
            }
        }
    }

    // TODO: Move attributeType / dynamic index field suffix to config
    // maybe make a bean taking care of this which could also do more
    // complex mapping like based on feature type column name defined suffix

    private static final String DEFAULT_FIELDSUFFIX = "_s";
    private static final String TREE_FIELD_SUFFIX = "_tree";
    private static final String FEATURE_FIELD_PREFIX = "ft_";
    private static final Map<String, String> XSDTYPES_TO_FIELD_NAME_SUFFIX;
    static { XSDTYPES_TO_FIELD_NAME_SUFFIX = ImmutableMap.<String, String>builder()
        .put("integer", "_ti")
        .put("string", DEFAULT_FIELDSUFFIX)
        .put("double", "_d")
        .put("boolean", "_b")
        .put("date", "_dt")
        .put("dateTime", "_dt")
        .build();
    }

    private Map<String, String> featureAttributeToDocumentFieldNames = new LinkedHashMap<String, String>();

    private void initFeatureAttributeToDocumentFieldNamesMapping(Map<String, String> featureAttributes, List<String> treeFields, Report report) {
        for (String attributeName : featureAttributes.keySet()) {
            String attributeType = featureAttributes.get(attributeName);
            if (attributeType.equals("geometry")) {
                featureAttributeToDocumentFieldNames.put(attributeName, "geom");
            } else {
                boolean isTree = treeFields != null ? treeFields.contains(attributeName) : false;
                featureAttributeToDocumentFieldNames.put(
                    attributeName,
                    String.join("",
                        FEATURE_FIELD_PREFIX,
                        attributeName,
                        XSDTYPES_TO_FIELD_NAME_SUFFIX.get(attributeType),
                        (isTree ? TREE_FIELD_SUFFIX : ""))
                );
            }
        }
        report.put("ftColumns_s", Joiner.on("|").join(featureAttributes.keySet()));
        report.put("docColumns_s", Joiner.on("|").join(featureAttributeToDocumentFieldNames.values()));
    }

    private String getDocumentFieldName(String attributeName) {
        return featureAttributeToDocumentFieldNames.get(attributeName);
    }
}
