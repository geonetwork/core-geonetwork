package org.fao.geonet.api.records.model.related;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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

