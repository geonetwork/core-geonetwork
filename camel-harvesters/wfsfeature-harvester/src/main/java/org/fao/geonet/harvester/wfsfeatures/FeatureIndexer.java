package org.fao.geonet.harvester.wfsfeatures;

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

    private SolrClient solr;

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

        linkage = linkage.replaceFirst("^(http://|https://)", "");

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

                    // Only index fields from config
                    if(fieldsConfig != null && fieldsConfig.get(attributeName) != null) {
                        if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                            String attributeValue = node.getTextContent();
                            if (attributeValue != null) {
                                attributeValue = attributeValue.trim();
                                // TODO: Get geometry
                                // TODO: Visit complex features ?
                                String fieldType;
                                if(fieldsConfig.get(attributeName).equals("integer")) {
                                    fieldType = "_i";
                                    //TODO: maybe use _ti for better intervals performances
                                }
                                else {
                                    fieldType = "_s";
                                }
                                document.addField(attributeName + fieldType, attributeValue);
                            }
                        }
                    }
                }
            }
        }
        try {
            // Commit within 10s
            UpdateResponse response = solr.add(document, 10000);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
