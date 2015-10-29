package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.Exchange;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.UUID;

public class FeatureIndexer {
    private SolrClient solr;

    public void featureToIndexDocument(Exchange exchange) {
        Document feature = exchange.getIn().getBody(Document.class);

        String urlString = "http://localhost:8984/solr/srv-catalog";
        solr = new HttpSolrClient(urlString);

        SolrInputDocument document = new SolrInputDocument();
        String featureName = feature.getFirstChild().getLocalName();

        // TODO: Discuss unique IDs
        document.addField("id", featureName + "_" + UUID.randomUUID());
        document.addField("docType", "feature");

        NodeList featureAttributes = feature.getFirstChild().getChildNodes();
        if (featureAttributes != null && featureAttributes.getLength() > 0) {
            for (int i = 0; i < featureAttributes.getLength(); i++) {
                // TODO: Discuss index attributes ?
                if (featureAttributes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element attribute = (Element) featureAttributes.item(i);
                    String attributeName = attribute.getLocalName();
                    Node node = attribute.getFirstChild();
                    if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                        String attributeValue = node.getTextContent();
                        if (attributeValue != null) {
                            attributeValue = attributeValue.trim();
                            // TODO: Get geometry
                            // TODO: Visit complex features ?
                            document.addField(attributeName + "_s", attributeValue);
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
