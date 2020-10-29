package org.fao.geonet.schemas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class XslIndexTest {

    @Test
    @Ignore
    public void nominal() throws Exception {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        Element metadata = Xml.loadFile(this.getClass().getResource("xsl/process/input.xml"));
        Path styleSheetToTest = Paths.get(this.getClass().getResource("/index-fields/index.xsl").toURI());
        Element xmlDoc = Xml.transform(metadata, styleSheetToTest);
        ObjectNode jsonDoc = toJson(xmlDoc);
        HashMap result = new ObjectMapper().convertValue(jsonDoc, HashMap.class);
        assertEquals(3, ((List)result.get("linkUrl")).size());
        assertEquals("FAO - NRCW", result.get("Org"));
    }

    private ObjectNode toJson(Element xmlDoc) {
        ObjectNode doc = new ObjectMapper().createObjectNode();

        List<String> elementNames = new ArrayList();
        List<Element> fields = xmlDoc.getChildren();

        // Loop on doc fields
        for (Element currentField: fields) {
            String name = currentField.getName();

            if (elementNames.contains(name)) {
                continue;
            }

            // Register list of already processed names
            elementNames.add(name);
            // Field starting with _ not supported in Kibana
            // Those are usually GN internal fields
            String propertyName = name.startsWith("_") ? name.substring(1) : name;
            List<Element> nodeElements = xmlDoc.getChildren(name);

            boolean isArray = nodeElements.size() > 1;
            if (isArray) {
                ArrayNode arrayNode = doc.putArray(propertyName);
                for (Element node : nodeElements) {
                    arrayNode.add(node.getTextNormalize());
                }
                continue;
            }
            if (name.equals("geom")) {
                continue;
            }

            if (name.equals("geojson")) {
                doc.put("geom", nodeElements.get(0).getTextNormalize());
                continue;
            }
            if (!name.startsWith("conformTo_")) { // Skip some fields causing errors / TODO
                doc.put(propertyName, nodeElements.get(0).getTextNormalize());
            }
        }
        return doc;
    }
}
