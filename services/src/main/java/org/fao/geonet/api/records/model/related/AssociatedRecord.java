package org.fao.geonet.api.records.model.related;

import com.fasterxml.jackson.databind.JsonNode;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedMetadataItem", propOrder = {
    "origin"
})
public class AssociatedRecord {
    @XmlElement()
    protected String origin;

    protected JsonNode record;
    private String uuid;
    private Map<String, String> properties = new HashMap<>();

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String value) {
        this.origin = value;
    }

    public JsonNode getRecord() {
        return record;
    }

    public void setRecord(JsonNode record) {
        this.record = record;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
