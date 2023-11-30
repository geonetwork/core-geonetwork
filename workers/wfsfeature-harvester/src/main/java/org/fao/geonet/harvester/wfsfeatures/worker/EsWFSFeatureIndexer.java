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
import org.apache.camel.Exchange;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.geotools.data.DataSourceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.rest.RestStatus.CREATED;
import static org.elasticsearch.rest.RestStatus.OK;


// TODO: GeoServer WFS 1.0.0 in some case return
// Feb 18, 2016 12:04:22 PM org.geotools.data.wfs.v1_0_0.NonStrictWFSStrategy createFeatureReaderGET
// WARNING: java.io.IOException: org.xml.sax.SAXException: cannot merge two target namespaces. http://www.openplans.org/topp http://www.openplans.org/spearfish


public class EsWFSFeatureIndexer {
    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_START_REGEX = "<!\\[CDATA\\[";
    public static final String CDATA_END = "]]>";
    private static Logger LOGGER = LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);

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
    private EsRestClient client;

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
     * <p>
     * Load configuration from exchange properties.
     * Could be a {@link WFSHarvesterParameter} or url and typeName
     * exchange properties.
     *
     * @param exchange
     * @param connect  Init datastore ie. connect to WFS, retrieve schema
     */
    public void initialize(Exchange exchange, boolean connect) throws InvalidArgumentException {
        WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
        if (configuration == null) {
            throw new InvalidArgumentException("Missing WFS harvester configuration.");
        }

        try {
            ((SpringCamelContext) exchange.getContext())
                .getApplicationContext()
                .getBean(EsSearchManager.class)
                .init(false, Optional.of(Arrays.asList("features")));
        } catch (Exception e) {
            LOGGER.error("Failed to create missing index for features. " + e.getMessage());
        }

        LOGGER.info("Initializing harvester configuration for uuid '{}', url '{}'," +
            "feature type '{}'. treefields are {}, tokenizedFields are {} Exchange id is '{}'.", new Object[]{
            configuration.getMetadataUuid(),
            configuration.getUrl(),
            configuration.getTypeName(),
            configuration.getTreeFields(),
            configuration.getTokenizedFields(),
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

    public void deleteFeatures(String url, String typeName, EsRestClient client) {
        LOGGER.info("Deleting features previously index from service '{}' and feature type '{}' in index '{}/{}'",
            new Object[]{url, typeName, index, indexType});
        try {
            long begin = System.currentTimeMillis();
            client.deleteByQuery(index, String.format("+featureTypeId:\"%s\"", getIdentifier(url, typeName)));
            LOGGER.info("  Features deleted in {} ms.", System.currentTimeMillis() - begin);

            begin = System.currentTimeMillis();
            client.deleteByQuery(index, String.format("+id:\"%s\"",
                getIdentifier(url, typeName)));
            LOGGER.info("  Report deleted in {} ms.", System.currentTimeMillis() - begin);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error connecting to ES at '{}'. Error is {}.", index, e.getMessage());
        }
    }

    interface TitleResolver {
        void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature);
    }

    public CompletableFuture<Void> indexFeatures(Exchange exchange) throws Exception {
        WFSHarvesterExchangeState state = (WFSHarvesterExchangeState) exchange.getProperty("featureTypeConfig");
        CompletableFuture<Void> future = new CompletableFuture<>();

        String url = state.getParameters().getUrl();
        String typeName = state.getParameters().getTypeName();
        List<String> resolvedTypeNames = state.getResolvedTypeNames();
        String strategyId = state.getStrategyId();

        Map<String, String> tokenizedFields = state.getParameters().getTokenizedFields();
        WFSDataStore wfs = state.getWfsDatastore();
        Map<String, String> featureAttributes = state.getFields();
        TitleResolver titleResolver = getTitleResolver(state);

        LOGGER.info("Indexing WFS features from service '{}' and feature type '{}'. Precision model applied: '{}', number of decimals: '{}'", url, typeName, applyPrecisionModel, numberOfDecimals);
        Report report = new Report(url, typeName);
        ObjectNode protoNode = createProtoNode(url, typeName);
        if (state.getParameters().getMetadataUuid() != null) {
            report.put("parent", state.getParameters().getMetadataUuid());
            protoNode.put("recordGroup", state.getParameters().getMetadataUuid());
            ObjectNode linkToParent = jacksonMapper.createObjectNode();
            linkToParent.put("name", "feature");
            linkToParent.put("parent", state.getParameters().getMetadataUuid());
            protoNode.set("featureOfRecord", linkToParent);
        }
        initFeatureAttributeToDocumentFieldNamesMapping(featureAttributes, state.getParameters().getTreeFields(), report);
        boolean initializeESReportSucceeded = report.saveHarvesterReport();
        if (!initializeESReportSucceeded) {
            String msg = "Couldn't initialize harvesting report, don't even try to go further querying wfs.";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }

        try {
            nbOfFeatures = 0;

            final Phaser phaser = new Phaser();
            BulkResutHandler brh = new AsyncBulkResutHandler(phaser, typeName, url, nbOfFeatures, report, state.getParameters().getMetadataUuid());

            long begin = System.currentTimeMillis();

            String epsg = "urn:ogc:def:crs:OGC:1.3:CRS84";

            for (String featureType : resolvedTypeNames) {
                SimpleFeatureCollection fc = wfs.getFeatureSource(featureType).getFeatures();

                ReprojectingFeatureCollection rfc = new ReprojectingFeatureCollection(fc, CRS.decode(epsg));

                FeatureIterator<SimpleFeature> features = rfc.features();

                try {
                    while (features.hasNext()) {
                        String featurePointer = String.format("%s#%s", featureType, nbOfFeatures);
                        try {
                            SimpleFeature feature = null;
                            try {
                                feature = features.next();
                                featurePointer = String.format("%s/id:%s", featurePointer, feature.getID());
                            } catch (Exception e) {
                                if (e.getCause() instanceof IOException
                                    || e.getCause() instanceof DataSourceException) {
                                    String msg = String.format(
                                        "Error while getting feature %s. Exception is: %s. Harvesting task will be stopped. This is probably a problem with the data source or some network related issues. Try to relaunch it later.",
                                        featurePointer,
                                        e.getMessage()
                                    );
                                    LOGGER.warn(msg);
                                    report.put("error_ss", msg);
                                    break;
                                }
                                String msg = String.format(
                                    "Error on reading %s. Exception is: %s",
                                    featurePointer, e.getMessage()
                                );
                                LOGGER.warn(msg);
                                report.put("error_ss", msg);
                                continue;
                            }
                            ObjectNode rootNode = protoNode.deepCopy();
                            titleResolver.setTitle(rootNode, feature);
                            rootNode.put("featureType", featureType);

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
                                        if (geom.isValid()) {
                                            PrecisionModel precisionModel = new PrecisionModel(Math.pow(10, numberOfDecimals - 1));
                                            geom = GeometryPrecisionReducer.reduce(geom, precisionModel);
                                            // numberOfDecimals is equal to
                                            // precisionModel.getMaximumSignificantDigits()
                                        } else {
                                            String msg = String.format(
                                                "Feature %s: Cannot apply precision reducer on invalid geometry. Check the geometry validity. The feature will be indexed but with no geometry.",
                                                featurePointer);
                                            LOGGER.warn(msg);
                                            report.put("error_ss", msg);
                                            break;
                                        }
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
                                    rootNode.set(getDocumentFieldName(attributeName), jsonNode);

                                    boolean isPoint = geom instanceof Point;
                                    if (isPoint) {
                                        Coordinate point = geom.getCoordinate();
                                        rootNode.put("location", String.format("%s,%s", point.y, point.x));
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
                                } else if (attributeValue instanceof Instant) {
                                    try {
                                        Position position = ((DefaultInstant) attributeValue).getPosition();

                                        if (position != null && position.getDate() != null) {
                                            rootNode.put(getDocumentFieldName(attributeName),
                                                position.getDate().toInstant().toString());
                                        }
                                    } catch (Exception instantException) {
                                        String msg = String.format(
                                            "Feature %s: Cannot read attribute %s, value %s. Exception is: %s",
                                            featurePointer, attributeName, attributeValue, instantException.getMessage());
                                        LOGGER.warn(msg);
                                        report.put("error_ss", msg);
                                    }
                                } else {
                                    String value = attributeValue.toString();
                                    rootNode.put(getDocumentFieldName(attributeName),
                                        value.startsWith(CDATA_START) ?
                                            value.replaceFirst(CDATA_START_REGEX, "").substring(0, value.length() - CDATA_END.length() - CDATA_START.length()) :
                                            value

                                    );
                                }
                            }

                            nbOfFeatures++;
                            brh.addAction(rootNode, feature);

                        } catch (Exception ex) {
                            String msg = String.format(
                                "Feature %s: Error is: %s",
                                featurePointer, ex.getMessage()
                            );
                            LOGGER.warn(msg);
                            report.put("error_ss", msg);
                        }

                        if (brh.getBulkSize() >= featureCommitInterval) {
                            brh.launchBulk(client);
                            brh = new AsyncBulkResutHandler(phaser, typeName, url, nbOfFeatures, report, state.getParameters().getMetadataUuid());
                        }
                    }
                } finally {
                    features.close();
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
            LOGGER.info("{}: {} features processed in {} ms.", new Object[]{
                typeName, nbOfFeatures,
                System.currentTimeMillis() - begin
            });
            report.success(nbOfFeatures);
        } catch (Exception e) {
            report.put("status_s", "error");
            report.put("error_ss", e.getMessage());
            LOGGER.error(e.getMessage());
            throw e;
        } finally {
            report.saveHarvesterReport();
            future.complete(null);
        }

        return future;
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
        } else if (defaultTitleAttribute != null) {
            titleResolver = new TitleResolver() {
                @Override
                public void setTitle(ObjectNode objectNode, SimpleFeature simpleFeature) {
                    Object titleAttribute = simpleFeature.getAttribute(defaultTitleAttribute);
                    if (titleAttribute != null) {
                        objectNode.put("resourceTitle", titleAttribute.toString());
                    }
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
            report.put("status_s", "success");
            report.put("totalRecords_i", nbOfFeatures);
            OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
            report.put("endDate_dt", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            report.put("isPointOnly", pointOnlyForGeoms);

        }

        public boolean saveHarvesterReport() {
            IndexRequest request = new IndexRequest(index);
            request.id(report.get("id").toString());
            request.source(report);
            try {
                IndexResponse response = client.getClient().index(request, RequestOptions.DEFAULT);
                if (response.status() == RestStatus.CREATED || response.status() == RestStatus.OK) {
                    LOGGER.info("Report saved for service {} and typename {}. Report id is {}",
                        url, typeName, report.get("id"));
                } else {
                    LOGGER.info("Failed to save report for {}. Error was '{}'.",
                        typeName,
                        response.getResult());
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

    abstract class BulkResutHandler {

        protected Phaser phaser;
        protected String typeName;
        private String url;
        protected int firstFeatureIndex;
        private Report report;
        private String metadataUuid;
        protected long begin;
        protected BulkRequest bulk;
        protected int bulkSize;
        protected int failuresCount;
        ActionListener<BulkResponse> listener;

        public BulkResutHandler(Phaser phaser, String typeName, String url, int firstFeatureIndex, Report report, String metadataUuid) {
            this.phaser = phaser;
            this.typeName = typeName;
            this.url = url;
            this.firstFeatureIndex = firstFeatureIndex;
            this.report = report;

            this.metadataUuid = metadataUuid;
            this.bulk = new BulkRequest(index);
            this.bulkSize = 0;
            this.failuresCount = 0;
            LOGGER.debug("  {} - Indexing bulk (size {}) starting at {} ...",
                typeName, featureCommitInterval, firstFeatureIndex);

            listener = new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkResponse) {
                    AtomicInteger bulkFailures = new AtomicInteger();
                    if (bulkResponse.hasFailures()) {
                        Arrays.stream(bulkResponse.getItems()).forEach(e -> {
                            if (e.status() != OK
                                && e.status() != CREATED) {
                                String msg = String.format(
                                    "Feature %s: Indexing error. Error is: %s", e.getId(), e.getFailure().toString());
                                report.put("error_ss", msg);
                                LOGGER.warn(msg);
                                bulkFailures.getAndIncrement();
                            }
                        });
                    }
                    LOGGER.debug("  {} - Features [{}-{}] indexed in {} ms{}.", new Object[]{
                        typeName, firstFeatureIndex, firstFeatureIndex + bulkSize,
                        System.currentTimeMillis() - begin,
                        bulkResponse.hasFailures() ?
                            " but with " + bulkFailures + " errors" : ""
                    });
                    failuresCount = bulkFailures.get();
                    phaser.arriveAndDeregister();
                }

                @Override
                public void onFailure(Exception e) {
                    String msg = String.format(
                        "Features [%s-%s] indexed in %s ms but with errors. Exception: %s",
                        typeName, firstFeatureIndex, bulkSize,
                        System.currentTimeMillis() - begin,
                        e.getMessage()
                    );
                    report.put("error_ss", msg);
                    LOGGER.error(msg);
                    phaser.arriveAndDeregister();
                }
            };
        }

        public int getBulkSize() {
            return bulkSize;
        }

        public int getNumberOfIndexedFeatures() {
            return bulkSize - failuresCount;
        }

        public void addAction(ObjectNode rootNode, SimpleFeature feature) throws JsonProcessingException {
            // generate a unique feature id when geotools gives us a placeholder one
            String featureId = feature.getID();
            if (featureId.toLowerCase().indexOf("placeholder") > -1) {
                featureId = "fid-" + nbOfFeatures;
            }

            String id = String.format("%s#%s#%s", url, typeName, featureId);
            bulk.add(new IndexRequest(index).id(id)
                .source(jacksonMapper.writeValueAsString(rootNode), XContentType.JSON));
//                .routing(ROUTING_KEY));
            bulkSize++;
        }

        protected void prepareLaunch() {
            phaser.register();
            this.begin = System.currentTimeMillis();
        }

        abstract public void launchBulk(EsRestClient client) throws Exception;
    }

    // depending on situation, one can expect going up to 1.5 faster using an async result handler (e.g. hudge collection of points)
    class AsyncBulkResutHandler extends BulkResutHandler {
        public AsyncBulkResutHandler(Phaser phaser, String typeName, String url, int firstFeatureIndex, Report report, String metadataUuid) {
            super(phaser, typeName, url, firstFeatureIndex, report, metadataUuid);
        }

        public void launchBulk(EsRestClient client) throws Exception {
            prepareLaunch();
            client.getClient().bulkAsync(this.bulk, RequestOptions.DEFAULT, this.listener);
        }
    }

    // TODO: Move attributeType / dynamic index field suffix to config
    // maybe make a bean taking care of this which could also do more
    // complex mapping like based on feature type column name defined suffix

    private static final String DEFAULT_FIELDSUFFIX = "_s";
    private static final String TREE_FIELD_SUFFIX = "_tree";
    private static final String FEATURE_FIELD_PREFIX = "ft_";
    private static final Map<String, String> XSDTYPES_TO_FIELD_NAME_SUFFIX;

    static {
        XSDTYPES_TO_FIELD_NAME_SUFFIX = ImmutableMap.<String, String>builder()
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
                boolean isTree = treeFields != null && treeFields.contains(attributeName);
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
