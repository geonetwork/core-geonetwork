package org.fao.geonet.kernel.csw.services.getrecords.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.csw.services.getrecords.FilterParser;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.jdom.Element;
import org.junit.jupiter.api.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CswFilter2EsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Example test on how to use Jackson to compare two JSON Objects.
     * 
     * @throws IOException
     */
    @Test
    void testJSONParser() throws IOException {

        final JsonNode root1 = MAPPER.readTree(new StringReader("{ \"query\": 23, \"query2\": 42}"));
        final JsonNode root2 = MAPPER.readTree(new StringReader("{ \"query2\": 42, \"query\": 23 }"));
        assertEquals(root1, root2);

        final ObjectNode root3 = (ObjectNode) MAPPER.createObjectNode();
        root3.put("query", 23);
        root3.put("query2", 42);
        assertEquals(root1, root3);
    }
}
