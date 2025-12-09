package org.fao.geonet.api.regions.model;

import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Created by francois on 21/06/16.
 */
public class Category {
    private String id;
    private String label;

    public Category(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
