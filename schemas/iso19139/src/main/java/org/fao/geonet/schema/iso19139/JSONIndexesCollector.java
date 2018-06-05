package org.fao.geonet.schema.iso19139;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.saxon.om.Item;

public class JSONIndexesCollector {

    public ObjectNode doc = new ObjectMapper().createObjectNode();

    public JSONIndexesCollector() {
    }

    public void addIndex(String key, Object val) {
        if (val == null) {
            val = "";
        }
        if (val instanceof Item) {
            val = ((Item) val).getStringValue();
        }
        JsonNode alreadyDefined = doc.findValue(key);
        if (alreadyDefined == null) {
            doc.put(key, val.toString());
            return;
        }
        if (alreadyDefined.getNodeType() != JsonNodeType.ARRAY) {
            String valueToCopy = alreadyDefined.textValue();
            doc.remove(key);
            ArrayNode arrayNode = doc.putArray(key);
            arrayNode.add(valueToCopy);
            alreadyDefined = arrayNode;
        }

        ((ArrayNode)alreadyDefined).add(val.toString());
    }
}
