package org.fao.geonet.harvester.wfsfeatures;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.Exchange;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FeatureIndexer {
    private SolrClient solr;

    /**
     * Define for each attribute type the Solr field suffix.
     */
    private static Map<String, String> XSDTYPES_TO_SOLRFIELDSUFFIX;

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

    public FeatureIndexer setSolrCommitWithinMs(int solrCommitWithinMs) {
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


    public void deleteFeatures(Exchange exchange) {
        FeatureTypeConfig ftConfig = (FeatureTypeConfig) exchange.getProperty("featureTypeConfig");
        String wfsUrl = ftConfig.getWfsUrl();
        String featureTypeName = ftConfig.getFeatureType();

        wfsUrl = checkParameters(wfsUrl, featureTypeName);

        solr = new HttpSolrClient(solrCollectionUrl);
        try {
            solr.deleteByQuery(String.format(
                    "+featureTypeId:\"%s#%s\"", wfsUrl, featureTypeName)
            );
            solr.commit();
        } catch (SolrServerException e) {
            // TODO: Log errors
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void featureToIndexDocument(Exchange exchange) {

        FeatureTypeConfig ftConfig = (FeatureTypeConfig) exchange.getProperty("featureTypeConfig");
        String wfsUrl = ftConfig.getWfsUrl();
        String featureTypeName = ftConfig.getFeatureType();

        // TODO: Define better exception handling.
        wfsUrl = checkParameters(wfsUrl, featureTypeName);

        WFSDataStore wfs = ftConfig.getWfsDatastore();
        Map<String, String> fields = ftConfig.getFields();


        solr = new HttpSolrClient(solrCollectionUrl);

        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> source = wfs.getFeatureSource(featureTypeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = source.getFeatures();

            final FeatureIterator<SimpleFeature> features = featuresCollection.features();
            int numInBatch = 0, nbOfFeature = 0;
            Collection<SolrInputDocument> docCollection = new ArrayList<SolrInputDocument>();

            while (features.hasNext()) {
                SimpleFeature feature = features.next();

                SolrInputDocument document = new SolrInputDocument();

                document.addField("id", feature.getID());
                document.addField("docType", "feature");
                document.addField("featureTypeId", wfsUrl + "#" + featureTypeName);

                for (String attributeName : fields.keySet()) {
                    String attributeType = fields.get(attributeName);
                    Object attributeValue = feature.getAttribute(attributeName);
                    if (attributeValue != null) {
                        document.addField(attributeName + XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType), attributeValue);
                    }
                }

                docCollection.add(document);
                numInBatch++;
                if (numInBatch >= featureCommitInterval) {
                    UpdateResponse response = solr.add(docCollection, solrCommitWithinMs);
                    docCollection.clear();
                    numInBatch = 0;
                    // TODO : better log
                    System.out.println(" Commit.");
                }
                System.out.print(".");
            }
            if (docCollection.size() > 0) {
                UpdateResponse response = solr.add(docCollection, solrCommitWithinMs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
            // TODO: Log
        }
    }

    private String checkParameters(String wfsUrl, String featureTypeName) {
        if (StringUtils.isEmpty(wfsUrl)) {
            throw new IllegalArgumentException("Empty WFS server URL is not allowed.");
        }
        wfsUrl = wfsUrl != null ? wfsUrl.replaceFirst("^(http://|https://)", "") : "";

        if (StringUtils.isEmpty(featureTypeName)) {
            throw new IllegalArgumentException("Empty WFS type name is not allowed.");
        }
        return wfsUrl;
    }
}
