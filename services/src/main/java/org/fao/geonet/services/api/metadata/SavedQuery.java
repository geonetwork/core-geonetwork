package org.fao.geonet.services.api.metadata;

import org.jdom.Namespace;

import java.util.List;

/**
 * Created by francois on 29/01/16.
 */
public class SavedQuery {
    private String id;
    private String xpath;
    private List<Namespace> namespaces;

    public SavedQuery(String id, String xpath, List<Namespace> namespaces) {
        this.id = id;
        this.xpath = xpath;
        this.namespaces = namespaces;
    }
    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
