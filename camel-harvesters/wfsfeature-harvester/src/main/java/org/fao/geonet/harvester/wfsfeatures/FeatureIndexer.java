package org.fao.geonet.harvester.wfsfeatures;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.Exchange;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.fao.geonet.kernel.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
                .put("integer", "_i")
                .put("character", DEFAULT_SOLRFIELDSUFFIX)
                .put("real", "_d")
                .put("boolean", "_b")
                .put("date", "_dt")
                .put("datetime", "_dt")
                .build();
    }
    @Autowired
    private DataManager dataManager;

    private final String ID_DELIMITER = "||";

    public void featureToIndexDocument(Exchange exchange) {

        Document feature = exchange.getIn().getBody(Document.class);
        String uuid = (String)exchange.getProperty("mduuid");
        String linkage = (String)exchange.getProperty("linkage");
        String featureName = feature.getFirstChild().getLocalName();

        //TODO: use describeFeatureType for init ?
        FeatureTypeConfig featureTypeConfig = new FeatureTypeConfig(dataManager);
        Map<String, String> fieldsConfig = null;
        try {
            featureTypeConfig.load(uuid, linkage, featureName);
            fieldsConfig = featureTypeConfig.getFields();
        } catch (Exception e) {
            //TODO: log
            e.printStackTrace();
        }

        // NPE ?
        linkage = linkage != null ? linkage.replaceFirst("^(http://|https://)", "") : "";

        // TODO move to config
        String urlString = "http://localhost:8984/solr/srv-catalog";
        solr = new HttpSolrClient(urlString);

        SolrInputDocument document = new SolrInputDocument();

        // TODO: Discuss unique IDs
        document.addField("id", featureName + ID_DELIMITER + UUID.randomUUID());
        document.addField("docType", "feature");
        document.addField("fid", featureName + ID_DELIMITER + linkage);

        NodeList featureAttributes = feature.getFirstChild().getChildNodes();
        if (featureAttributes != null && featureAttributes.getLength() > 0) {
            for (int i = 0; i < featureAttributes.getLength(); i++) {
                // TODO: Discuss index attributes ?
                if (featureAttributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) featureAttributes.item(i);
                    String attributeName = attribute.getLocalName();
                    Node node = attribute.getFirstChild();

                    // Only index fields from config or all if no config.
                    if((fieldsConfig == null || fieldsConfig.size() == 0) ||
                       (fieldsConfig != null && fieldsConfig.get(attributeName) != null)) {
                        if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                            String attributeValue = node.getTextContent();
                            if (attributeValue != null) {
                                attributeValue = attributeValue.trim();
                                // TODO: Get geometry
                                // TODO: Visit complex features ?
                                String attributeConfig = fieldsConfig != null ? fieldsConfig.get(attributeName) : DEFAULT_WFS_DATATYPE;
                                String fieldType = XSDTYPES_TO_SOLRFIELDSUFFIX.get(attributeConfig);
                                if (fieldType == null) {
                                    fieldType = DEFAULT_SOLRFIELDSUFFIX;
                                }
                                document.addField(attributeName + fieldType, attributeValue);
                            }
                        }
                    }
                }
            }
        }
        try {
            UpdateResponse response = solr.add(document, SOLR_COMMIT_WITHIN_MS);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
