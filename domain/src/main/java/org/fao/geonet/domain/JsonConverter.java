package org.fao.geonet.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.AttributeConverter;

public class JsonConverter implements AttributeConverter<ObjectNode, String> {
    @Override
    public String convertToDatabaseColumn(ObjectNode jsonNodes) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(jsonNodes);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ObjectNode convertToEntityAttribute(String s) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(s, ObjectNode.class);
        } catch (Exception e) {
            return null;
        }
    }
}
