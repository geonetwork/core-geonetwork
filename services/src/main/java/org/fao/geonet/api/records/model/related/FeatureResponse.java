package org.fao.geonet.api.records.model.related;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relatedResponse")
@XmlRootElement(name = "related")
public class FeatureResponse {

    protected Map<String, String[]> decodeMap = new HashMap<>();

    public Map<String, String[]> getDecodeMap() {
        return decodeMap;
    }

    public void setDecodeMap(Map<String, String[]> decodeMap) {
        this.decodeMap = decodeMap;
    }

}

