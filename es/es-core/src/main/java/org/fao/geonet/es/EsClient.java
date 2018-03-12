/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.es;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Client to connect to Elasticsearch
 */
public class EsClient implements InitializingBean {
    private static Logger LOGGER =  LoggerFactory.getLogger("geonetwork.harvest.wfs.features");

    private static EsClient instance;

    private JestClient client;

    @Value("${es.url}")
    private String serverUrl;
    private String username;
    private String password;

    public static EsClient get() {
        return instance;
    }

    public JestClient getClient() throws Exception {
        return client;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (serverUrl != null) {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig
                .Builder(this.serverUrl)
                .multiThreaded(true)
                .readTimeout(-1)
                .build());
            client = factory.getObject();
//            Depends on java.lang.NoSuchFieldError: LUCENE_5_2_1
//            client = new PreBuiltTransportClient(Settings.EMPTY)
//                .addTransportAddress(new InetSocketTransportAddress(
//                    InetAddress.getByName("127.0.0.1"), 9300));

            synchronized (EsClient.class) {
                instance = this;
            }
        } else {
            throw new Exception(String.format(
                "No ES client URL defined in %s. "
                + "Check bean configuration.", this.serverUrl));
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public EsClient setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public EsClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public EsClient setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean bulkRequest(String index, Map<String, String> docs) throws IOException {
        boolean success = true;
        Bulk.Builder bulk = new Bulk.Builder()
            .defaultIndex(index)
            .defaultType(index);

        Iterator iterator = docs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) iterator.next();
            bulk.addAction(
                new Index.Builder(entry.getValue()).id(entry.getKey()).build()
            );
        }
        try {
            BulkResult result = client.execute(bulk.build());
            if (!result.isSucceeded()) {
                System.out.println(result.getErrorMessage());
                System.out.println(result.getJsonString());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return success;
    }


    public void bulkRequestAsync(Bulk.Builder bulk , JestResultHandler<BulkResult> handler) {
        client.executeAsync(bulk.build(), handler);

    }

    public BulkResult bulkRequestSync(Bulk.Builder bulk) throws IOException {
        return client.execute(bulk.build());
    }

    public String deleteByQuery(String index, String query) throws Exception {

        String searchQuery = "{\"query\": {\"query_string\": {" +
            "\"query\": \"" + query + "\"" +
            "}}}";

        DeleteByQuery deleteAll = new DeleteByQuery.Builder(searchQuery)
            .addIndex(index)
            .addType(index)
            .build();
        final JestResult result = client.execute(deleteAll);
        if (result.isSucceeded()) {
            return String.format("Record removed. %s.", result.getJsonString());
        } else {
            throw new IOException(String.format(
                "Error during removal. Errors is '%s'.", result.getErrorMessage()
            ));
        }
    }

    protected void finalize() {
        client.shutdownClient();
    }
}
