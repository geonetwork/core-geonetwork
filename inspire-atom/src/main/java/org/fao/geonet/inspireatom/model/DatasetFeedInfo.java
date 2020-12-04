package org.fao.geonet.inspireatom.model;

/**
 * Dataset feed information.
 */
public class DatasetFeedInfo {
    public String identifier;
    public String namespace;
    public String feedUrl;

    public DatasetFeedInfo(String identifier, String namespace, String feedUrl) {
        this.identifier = identifier;
        this.namespace = namespace;
        this.feedUrl = feedUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFeedUrl() {
        return feedUrl;
    }
}
