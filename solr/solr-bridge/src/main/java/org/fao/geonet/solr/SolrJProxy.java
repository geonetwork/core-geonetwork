package org.fao.geonet.solr;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Create a bean providing a connection to the
 * Solr.
 */
@Component
public class SolrJProxy implements InitializingBean {

    private static SolrJProxy instance;
    private SolrClient client;

    @Autowired
    private SolrConfig config;

    private boolean connectionChecked = false;

    /**
     * The first time this method is called, ping the
     * client to check connection status.
     *
     * @return The Solr instance.
     */
    public SolrClient getServer() throws Exception {
        if (!connectionChecked) {
            this.ping();
            connectionChecked = true;
        }
        return client;
    }

    public SolrJProxy setServer(SolrClient server) {
        this.client = server;
        return this;
    }

    /**
     * Connect to the Solr, ping the client
     * to check connection and set the instance.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (config.getSolrServerUrl() != null) {
            String url = config.getSolrServerUrl() + "/" +
                    config.getSolrServerCore();
            if (!StringUtils.isEmpty(config.getSolrServerUsername()) &&
                    !StringUtils.isEmpty(config.getSolrServerPassword())) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                config.getSolrServerUsername(),
                                config.getSolrServerPassword()));
                CloseableHttpClient httpClient =
                        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
                client = new HttpSolrClient(url, httpClient);
            } else {
                client = new HttpSolrClient(url);
            }

            instance = this;
        } else {
            throw new Exception("No Solr URL defined. Check configuration.");
        }
    }

    /**
     * Ping the Solr.
     *
     * @throws Exception
     */
    public void ping() throws Exception {
        try {
            client.ping();
        } catch (Exception e) {
            throw new Exception(
                    String.format("Failed to ping Solr at URL %s. " +
                                    "Check configuration.",
                            config.getSolrServerUrl()),
                    e);
        }
    }

    /**
     * @return Return the bean instance
     */
    public static SolrJProxy get() {
        return instance;
    }
}