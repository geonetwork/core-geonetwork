/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.solr;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

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

    @Nonnull
    public SolrClient createClient() throws Exception {
        if (solrServerUrl != null) {
            String url = solrServerUrl + "/" + solrServerCore;
            if (!StringUtils.isEmpty(solrServerUsername) &&
                    !StringUtils.isEmpty(solrServerPassword)) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                                solrServerUsername,
                                solrServerPassword));
                CloseableHttpClient httpClient =
                        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
                return new HttpSolrClient(url, httpClient);
            } else {
                return new HttpSolrClient(url);
            }
        } else {
            throw new Exception("No Solr URL defined. Check configuration.");
        }
    }
}
