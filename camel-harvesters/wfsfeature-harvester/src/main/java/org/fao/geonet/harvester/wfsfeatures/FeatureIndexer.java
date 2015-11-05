package org.fao.geonet.harvester.wfsfeatures;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.Exchange;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.fao.geonet.kernel.DataManager;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

@Component
public class FeatureIndexer {

    // TODO move to config
    public static final int SOLR_COMMIT_WITHIN_MS = 10000;
    public static final String DEFAULT_WFS_DATATYPE = "character";
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
    @Autowired
    private DataManager dataManager;

    private final String ID_DELIMITER = "||";

    public void featureToIndexDocument(Exchange exchange) {

        FeatureTypeConfig ftConfig = (FeatureTypeConfig)exchange.getProperty("featureTypeConfig");
        String wfsUrl = ftConfig.getWfsUrl();
        WFSDataStore wfs = ftConfig.getWfsDatastore();
        String featureTypeName = ftConfig.getFeatureType();
        Map<String, String> fields = ftConfig.getFields();

        wfsUrl = wfsUrl != null ? wfsUrl.replaceFirst("^(http://|https://)", "") : "";

        // TODO move to config
        String urlString = "http://localhost:8984/solr/srv-catalog";
        solr = new HttpSolrClient(urlString);

        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> source = wfs.getFeatureSource(featureTypeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = source.getFeatures();

            final FeatureIterator<SimpleFeature> features = featuresCollection.features();
            int numInBatch = 0;
            Collection<SolrInputDocument> docCollection = new ArrayList<SolrInputDocument>();

            while(features.hasNext()) {
                SimpleFeature feature = features.next();

                SolrInputDocument document = new SolrInputDocument();

                // TODO: Discuss unique IDs
                document.addField("id", feature.getID());
                document.addField("docType", "feature");
                document.addField("featureTypeId", wfsUrl + "#" + featureTypeName);

                for (String attributeName : fields.keySet()) {
                    String attributeType = fields.get(attributeName);
                    Object attributeValue = feature.getAttribute(attributeName);
                    if(attributeValue != null) {
                        document.addField(attributeName + XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeType), attributeValue);
                    }
                }

                docCollection.add(document);
                numInBatch++;
                if (numInBatch >= 200) {
                    UpdateResponse response = solr.add(docCollection, SOLR_COMMIT_WITHIN_MS);
                    docCollection.clear();
                    numInBatch = 0;
                }
            }
            UpdateResponse response = solr.add(docCollection, SOLR_COMMIT_WITHIN_MS);
        }
        catch (IOException e) {
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }
}
