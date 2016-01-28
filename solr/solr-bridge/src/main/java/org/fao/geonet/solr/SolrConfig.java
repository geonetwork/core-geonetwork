package org.fao.geonet.solr;

/**
 */
public class SolrConfig {

    private String solrServerUrl;
    private String solrServerCore;
    private String solrServerUsername;
    private String solrServerPassword;

    /**
     * @return Return the Solr URL
     */
    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    /**
     * @param solrServerUrl The Solr URL
     */
    public SolrConfig setSolrServerUrl(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
        return this;
    }

    /**
     * @return Return the Solr core to connect to
     */
    public String getSolrServerCore() {
        return solrServerCore;
    }

    /**
     * @param solrServerCore The Solr core to connect to
     */
    public SolrConfig setSolrServerCore(String solrServerCore) {
        this.solrServerCore = solrServerCore;
        return this;
    }

    /**
     * @return Return Solr username for credentials
     */
    public String getSolrServerUsername() {
        return solrServerUsername;
    }

    /**
     * @param solrServerUsername The Solr credentials username
     */
    public SolrConfig setSolrServerUsername(String solrServerUsername) {
        this.solrServerUsername = solrServerUsername;
        return this;
    }

    /**
     * @return Return Solr password for credentials
     */
    public String getSolrServerPassword() {
        return solrServerPassword;
    }

    /**
     * @param solrServerPassword The Solr credentials password
     */
    public SolrConfig setSolrServerPassword(String solrServerPassword) {
        this.solrServerPassword = solrServerPassword;
        return this;
    }
}