package org.fao.geonet.kernel.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

public class EsSearchManagerTest {

    private EsSearchManager instance;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.instance = new EsSearchManager();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void documentToJsonSimple() throws JsonProcessingException {
        Element input = new Element("doc");
        Element field1 = new Element("field1").setText("content1");
        Element field2 = new Element("field2").setText("content2");
        input.addContent(field1);
        input.addContent(field2);

        ObjectNode result = instance.documentToJson(input);
        JsonNode expected = objectMapper.readTree(" {\"field1\":\"content1\",\"field2\":\"content2\"}");

        assertEquals(expected, result);
    }

    @Test
    public void documentToJsonIndexingErrorMsg() throws JsonProcessingException {
        Element input = new Element("doc");
        Element field1 = new Element("field1").setText("content1");
        String escapedJson = "{\n" +
            "  \"type\": \"error\",\n" +
            "  \"string\": \"string-reference\",\n" +
            "  \"values\": {\n" +
            "    \"value1\": \"content1\",\n" +
            "    \"value2\": \"content2\"\n" +
            "  }\n" +
            "}";
        Element field2 = new Element(IndexFields.INDEXING_ERROR_MSG).setText(escapedJson);
        Element nestedField1 = new Element("nestedField1").setText("nestedContent1");
        Element nestedField2 = new Element("nestedField2").setText("nestedcontent2");
        field2.addContent(nestedField1);
        field2.addContent(nestedField2);
        field2.setAttribute("type", "object");

        input.addContent(field1);
        input.addContent(field2);

        ObjectNode result = instance.documentToJson(input);
        JsonNode expected = objectMapper.readTree("{\"field1\":\"content1\",\"indexingErrorMsg\":[{\"type\":\"error\",\"string\":\"string-reference\",\"values\":{\"value1\":\"content1\",\"value2\":\"content2\"}}]}");

        assertEquals(expected, result);
    }
}
