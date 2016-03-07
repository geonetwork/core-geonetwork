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
import com.google.common.collect.ImmutableSet;
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
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolrWFSFeatureIndexer {
    public static final String MULTIVALUED_SUFFIX = "s";
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

    /**
     * A list of properties to be saved in the index
     */
    private static Map<String, Object> harvesterReportFields = new HashMap<>();

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

    private void saveHarvesterReport() {
        solr = new HttpSolrClient(solrCollectionUrl);
        SolrInputDocument harvestingTaskDocument = new SolrInputDocument();
        Iterator<String> fields = harvesterReportFields.keySet().iterator();
        while (fields.hasNext()) {
            String field = fields.next();
            harvestingTaskDocument.addField(
                    field,
                    harvesterReportFields.get(field));
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
        Map<String, String> fields = state.getFields();

        solr = new HttpSolrClient(solrCollectionUrl);

        harvesterReportFields.put("id", url + "#" + typeName);
        harvesterReportFields.put("docType", "harvesterReport");

        final Map<String, String> tokenizedFields = state.getParameters().getTokenize();
        final boolean hasTokenizedFields = tokenizedFields != null;

        List<String> docColumns = new ArrayList<>(fields.size());
        for (String attributeName : fields.keySet()) {
            String attributeType = fields.get(attributeName);
            String separator = null;
            if (hasTokenizedFields) {
                separator = tokenizedFields.get(attributeName);
            }
            if (attributeType.equals("geometry")) {
                docColumns.add("geom");
            } else {
                boolean isTokenized = separator != null;
                docColumns.add(attributeName +
                               XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType) +
                               (isTokenized ? MULTIVALUED_SUFFIX : "")
                );
            }
        }
        harvesterReportFields.put("ftColumns_s", Joiner.on("|").join(fields.keySet()));
        harvesterReportFields.put("docColumns_s", Joiner.on("|").join(docColumns));
        if (state.getParameters().getMetadataUuid() != null) {
            harvesterReportFields.put("parent", state.getParameters().getMetadataUuid());
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

            saveHarvesterReport();

            String titleExpression = state.getParameters().getTitleExpression();
            String defaultTitleAttribute = titleExpression == null ? guessFeatureTitleAttribute(fields) : null;

            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                nbOfFeatures ++;
                SolrInputDocument document = new SolrInputDocument();

                document.addField("id", feature.getID());
                document.addField("docType", "feature");
                document.addField("resourceType", "feature");
                document.addField("featureTypeId", url + "#" + typeName);

                if (titleExpression != null) {
                    document.addField("resourceTitle",
                            buildFeatureTitle(feature, fields, titleExpression));
                }

                if (state.getParameters().getMetadataUuid() != null) {
                    document.addField("parent", state.getParameters().getMetadataUuid());
                }

                for (String attributeName : fields.keySet()) {
                    String attributeType = fields.get(attributeName);
                    Object attributeValue = feature.getAttribute(attributeName);

                    if (attributeValue != null) {
                        if (attributeType.equals("geometry")) {
                            try {
                             document.addField("geom", attributeValue);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
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
                                        attributeName + XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType) + MULTIVALUED_SUFFIX,
                                        token);
                                }
                            } else {
                                document.addField(
                                        attributeName + XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType),
                                        attributeValue);
                            }


                            if (defaultTitleAttribute != null &&
                                defaultTitleAttribute.equals(attributeName)) {
                                document.addField("resourceTitle", attributeValue);
                            }
                        }
                    }
                }

                docCollection.add(document);
                numInBatch++;
                if (numInBatch >= featureCommitInterval) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "  %d features to index.",
                                nbOfFeatures));
                    }
                    UpdateResponse response = solr.add(docCollection);
                    solr.commit();
                    docCollection.clear();
                    numInBatch = 0;
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "  %d features indexed.",
                                nbOfFeatures));
                    }
                }
            }
            if (docCollection.size() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(
                            "  %d features to index.",
                            nbOfFeatures));
                }
                UpdateResponse response = solr.add(docCollection);
                solr.commit();
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format(
                            "  %d features indexed.",
                            nbOfFeatures));
                }
            }
            logger.info(String.format("Total number of features indexed is %d.", nbOfFeatures));
            harvesterReportFields.put("status_s", "success");
            harvesterReportFields.put("totalRecords_i", nbOfFeatures);
            harvesterReportFields.put("endDate_dt",
                    ISODateTimeFormat.dateTime().print(new DateTime()));
        } catch (IOException e) {
            harvesterReportFields.put("status_s", "error");
            harvesterReportFields.put("error_s", e.getMessage());
            logger.error(e.getMessage());
            throw e;
        } catch (SolrServerException e) {
            harvesterReportFields.put("status_s", "error");
            harvesterReportFields.put("error_s", e.getMessage());
            logger.error(e.getMessage());
            throw e;
        } finally {
            saveHarvesterReport();
        }
    }

    private static Pattern pt = Pattern.compile("\\{\\{([^}]*)\\}\\}");
    /**
     * Build a title for the feature. The title expression could be
     * an attribute name or could contain expression were attributes
     * will be substituted. eg. "{{TITLE_FR}} ({{ID}})"
     *
     * @param feature   A simple feature
     * @param fields    List of columns
     * @param titleExpression A title expression based on one or more attributes
     * @return
     */
    public static String buildFeatureTitle(SimpleFeature feature,
                                    Map<String, String> fields,
                                    String titleExpression) {
        if (StringUtils.isNotEmpty(titleExpression)) {
            if (titleExpression.contains("{{")) {
                Matcher m = pt.matcher(titleExpression);
                while (m.find()) {
                    String attributeName = m.group(1);
                    String attributeValue = (String) feature.getAttribute(attributeName);
                    titleExpression = titleExpression.replaceAll(
                            "\\{\\{" + attributeName + "\\}\\}",
                            attributeValue);

                }
                return titleExpression;
            } else {
                String attributeValue = (String) feature.getAttribute(titleExpression);
                if (attributeValue != null) {
                    return attributeValue;
                } else {
                    return null;
                }
            }
        } else {
            for (String attributeName : fields.keySet()) {
                String attributeType = fields.get(attributeName);
                String attributeValue = (String) feature.getAttribute(attributeName);
                if (attributeValue != null && !attributeType.equals("geometry")) {
                    return attributeValue;
                }
            }
        }
        return null;
    }

    private static Pattern titleColumnShouldMatchPattern =
            Pattern.compile(
                    ".*(TITLE|LABEL|NAME|TITRE|NOM|LIBELLE).*",
                    Pattern.CASE_INSENSITIVE);

    /**
     * From the list of attributes try to find the best one
     * for a title. If not found, return the first attribute.
     * If none, return null.
     *
     * @param fields List of attributes
     * @return
     */
    public static String guessFeatureTitleAttribute(Map<String, String> fields) {
        Set<String> keySet = fields.keySet();
        for (String attributeName : keySet) {
            Matcher m = titleColumnShouldMatchPattern.matcher(attributeName);
            if (m.find()) {
                return attributeName;
            }
        }
        return keySet.size() > 0 ? (String)keySet.toArray()[0] : null;
    }
}
