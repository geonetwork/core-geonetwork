package org.fao.geonet.harvester.wfsfeatures.services;

import java.util.Map;

/**
 * Created by francois on 14/01/16.
 */
public class IndexConfigurationParameter {
    private String url;
    private String typeName;
    /**
     * List of fields to tokenize during
     * indexing.
     *
     * The key is the column name, the value is the separator.
     */
    private Map<String, String> tokenize;

    public Map<String, String> getTokenize() {
        return tokenize;
    }

    public void setTokenize(Map<String, String> tokenize) {
        this.tokenize = tokenize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typename) {
        this.typeName = typename;
    }

}
