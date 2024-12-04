package org.fao.geonet.session.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//https://stackoverflow.com/questions/39507125/changing-return-type-with-json-annotations
public class SetDeserializer extends JsonDeserializer<Set> {

    @Override
    public Set deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);
        Set<Object> resultSet = new HashSet<>();
        if (node != null) {
            if (node instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) node;
                Iterator<JsonNode> nodeIterator = arrayNode.iterator();
                while (nodeIterator.hasNext()) {
                    JsonNode elementNode = nodeIterator.next();
                    resultSet.add(mapper.readValue(elementNode.toString(), Object.class));
                }
            } else {
                resultSet.add(mapper.readValue(node.toString(), Object.class));
            }
        }
        return resultSet;
    }
}
