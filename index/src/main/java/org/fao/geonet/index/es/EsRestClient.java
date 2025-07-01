/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.index.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WrapperQuery;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.json.spi.JsonProvider;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * Client to connect to Elasticsearch
 */
public class EsRestClient implements InitializingBean {
    private static EsRestClient instance;

    private ElasticsearchClient client;

    private ElasticsearchAsyncClient asyncClient;


    private String serverUrl;

    @Value("${es.protocol}")
    private String serverProtocol;

    @Value("${es.host}")
    private String serverHost;

    @Value("${es.port}")
    private String serverPort;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;

    private boolean activated = false;

    public static EsRestClient get() {
        return instance;
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public ElasticsearchAsyncClient getAsynchClient() {
        return asyncClient;
    }

    public String getDashboardAppUrl() {
        return dashboardAppUrl;
    }

    public void setDashboardAppUrl(String dashboardAppUrl) {
        this.dashboardAppUrl = dashboardAppUrl;
    }

    @Value("${kb.url}")
    private String dashboardAppUrl;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(serverProtocol) || StringUtils.isBlank(serverHost) || StringUtils.isBlank(serverPort)) {
            Log.error("geonetwork.index", String.format(
                "Elasticsearch URL defined by serverProtocol='%s', serverHost='%s', serverPort='%s' is missing. "
                    + "Check configuration.", this.serverProtocol,this.serverHost,this.serverPort));
        }

        //build server URL
        serverUrl = serverProtocol + "://" + serverHost + ":" + serverPort;
        if (StringUtils.isNotEmpty(serverUrl)) {
            RestClientBuilder builder = RestClient.builder(new HttpHost(serverHost, Integer.parseInt(serverPort), serverProtocol));

            if (serverProtocol.startsWith("https")) {
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
                    null, new TrustStrategy() {
                        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            return true;
                        }
                    }).build();
                // skip hostname checks
                HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                SchemeIOSessionStrategy httpsIOSessionStrategy = new SSLIOSessionStrategy(sslContext, hostnameVerifier);

                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));

                    builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.useSystemProperties().setSSLContext(sslContext).setDefaultCredentialsProvider(credentialsProvider));
                } else {
                    builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.useSystemProperties().setSSLContext(sslContext));
                }
            } else {
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));

                    builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.useSystemProperties().setDefaultCredentialsProvider(credentialsProvider));
                } else {
                    builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.useSystemProperties());
                }
            }

            RestClient restClient = builder.build();

            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

            client = new ElasticsearchClient(transport);

            asyncClient = new ElasticsearchAsyncClient(transport);

            synchronized (EsRestClient.class) {
                instance = this;
            }
            activated = true;
        } else {
            Log.debug("geonetwork.index", String.format(
                "No Elasticsearch URL defined '%s'. "
                    + "Check configuration.", this.serverUrl));
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public EsRestClient setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public EsRestClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public EsRestClient setPassword(String password) {
        this.password = password;
        return this;
    }

    public static final String ROUTING_KEY = "101";

    public BulkResponse bulkRequest(String index, Map<String, String> docs) throws IOException {
        if (!activated) {
            throw new IOException("Index not yet activated.");
        }

        BulkRequest.Builder requestBuilder = new BulkRequest.Builder()
            .index(index)
            .refresh(Refresh.True);

        JsonpMapper jsonpMapper = client._transport().jsonpMapper();
        JsonProvider jsonProvider = jsonpMapper.jsonProvider();

        Iterator<Map.Entry<String, String>> iterator = docs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            JsonData jd = JsonData.from(jsonProvider.createParser(new StringReader(entry.getValue())), jsonpMapper);

            requestBuilder
                .operations(op -> op.index(idx -> idx.index(index)
                    .id(entry.getKey())
                    .document(jd)));
        }

        BulkRequest request = requestBuilder.build();

        try {
            return client.bulk(request);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

//
//    public void bulkRequestAsync(Bulk.Builder bulk , JestResultHandler<BulkResult> handler) {
//        client.executeAsync(bulk.build(), handler);
//
//    }
//
//    public BulkResult bulkRequestSync(Bulk.Builder bulk) throws IOException {
//        return client.execute(bulk.build());
//    }


    /**
     * Query using Lucene query syntax.
     */
    public SearchResponse query(String index, String luceneQuery, String filterQuery,
                                Set<String> includedFields, Map<String, String> scriptedFields,
                                int from, int size) throws Exception {
        return query(index, luceneQuery, filterQuery, includedFields, scriptedFields, from, size, null);
    }


    public SearchResponse query(String index, String luceneQuery, String filterQuery, Set<String> includedFields,
                                int from, int size) throws Exception {
        return query(index, luceneQuery, filterQuery, includedFields, new HashMap<>(), from, size, null);
    }

    public SearchResponse query(String index, String luceneQuery, String filterQuery, Set<String> includedFields,
                                Map<String, String> scriptedFields,
                                int from, int size, List<SortOptions> sort) throws Exception {

        Query.Builder queryBuilder = new Query.Builder();
        queryBuilder.queryString(new QueryStringQuery.Builder().query(luceneQuery).build());

        Query.Builder filterBuilder = null;
        if (StringUtils.isNotEmpty(filterQuery)) {
            filterBuilder = new Query.Builder();
            filterBuilder.queryString(new QueryStringQuery.Builder().query(filterQuery).build());
        }
        return query(index, queryBuilder, filterBuilder, includedFields, scriptedFields, from, size, sort);
    }

    /**
     * Query using JSON elastic query
     */
    public SearchResponse query(String index, JsonNode jsonQuery, Query.Builder postFilterBuilder,
                                Set<String> includedFields, Map<String, String> scriptedFields,
                                int from, int size) throws Exception {
        return query(index, jsonQuery, postFilterBuilder, includedFields, scriptedFields, from, size, null);
    }

    public SearchResponse query(String index, JsonNode jsonQuery, Query.Builder postFilterBuilder, Set<String> includedFields,
                                int from, int size) throws Exception {
        return query(index, jsonQuery, postFilterBuilder, includedFields, new HashMap<>(), from, size, null);
    }

    public SearchResponse query(String index, JsonNode jsonQuery, Query.Builder postFilterBuilder,
                                Set<String> includedFields, Map<String, String> scriptedFields,
                                int from, int size, List<SortOptions> sort) throws Exception {
        final Query.Builder query = new Query.Builder();

        WrapperQuery.Builder wrapperQueryBuilder = new WrapperQuery.Builder();
        wrapperQueryBuilder.query(Base64.getEncoder().encodeToString(String.valueOf(jsonQuery).getBytes()));
        query.wrapper(wrapperQueryBuilder.build());

        return query(index, query, postFilterBuilder, includedFields, scriptedFields, from, size, sort);
    }

    public SearchResponse query(String index, Query.Builder queryBuilder, Query.Builder postFilterBuilder,
                                Set<String> includedFields, Map<String, String> scriptedFields,
                                int from, int size, List<SortOptions> sort) throws Exception {
        if (!activated) {
            return null;
        }

        // TODOES: Add permission if index is gn-records
        // See EsHTTPProxy#addUserInfo
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
            .index(index)
            .from(from)
            .size(size)
            .query(queryBuilder.build())
            .trackTotalHits(th -> th.enabled(true))
            .source(sc -> sc.filter(f -> f.includes(new ArrayList<>(includedFields))));

        if (postFilterBuilder != null) {
            searchRequestBuilder.postFilter(postFilterBuilder.build());
        }

        if (MapUtils.isNotEmpty(scriptedFields)) {
            for (Map.Entry<String, String> scriptedField: scriptedFields.entrySet()) {
                ScriptField scriptField = ScriptField.of(
                    b -> b.script(sb -> sb.inline(is -> is.source(scriptedField.getValue())))
                );

                searchRequestBuilder.scriptFields(scriptedField.getKey(), scriptField);
            }
        }

        if (sort != null) {
            searchRequestBuilder.sort(sort);
        }

        SearchRequest searchRequest = searchRequestBuilder.build();

        try {
            return client.search(searchRequest, ObjectNode.class);

        } catch (ElasticsearchException esException) {
            Log.error("geonetwork.index", String.format(
                "Error during querying index. %s", esException.error().toString()));
            throw esException;
        }
    }


    public String deleteByQuery(String index, String query) throws Exception {
        if (!activated) {
            return "";
        }

        DeleteByQueryRequest request = DeleteByQueryRequest.of(
            b -> b.index(new ArrayList<>(Arrays.asList(index)))
                .q(query)
                .refresh(true));

        final DeleteByQueryResponse deleteByQueryResponse =
            client.deleteByQuery(request);


        if (deleteByQueryResponse.deleted() >= 0) {
            return String.format("Record removed. %s.", deleteByQueryResponse.deleted());
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            deleteByQueryResponse.failures().forEach(f -> stringBuilder.append(f.toString()));

            throw new IOException(String.format(
                "Error during removal. Errors are '%s'.", stringBuilder
            ));
        }
    }

    /**
     * Get the complete document from the index.
     * @param id For record index, use UUID.
     * @return the source as Map
     */
    public Map<String, Object> getDocument(String index, String id) throws Exception {
        if (!activated) {
            return Collections.emptyMap();
        }

        GetRequest request = GetRequest.of(
            b -> b.index(index).id(id)
        );

        GetResponse<ObjectNode> response = client.get(request, ObjectNode.class);

        if (response.found()) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(response.source(), Map.class);
        } else {
            throw new Exception(String.format("Document with id %s not found", id));
        }
    }

    /**
     * Query the index for a specific record and return values for a set of fields.
     */
    public Map<String, String> getFieldsValues(String index, String id, Set<String> fields, String language) throws Exception {
        if (!activated) {
            return Collections.emptyMap();
        }

        Map<String, String> fieldValues = new HashMap<>();
        Map<String, Object> sources = getDocument(index, id);

        for (String field : fields) {
            Object value = sources.get(field);
            if (value instanceof String) {
                fieldValues.put(field, (String) value);
            } else if (value instanceof Map && field.endsWith("Object")) {
                Map valueMap = (Map) value;
                String languageValue = (String) valueMap.get("lang" + language);
                fieldValues.put(field, languageValue != null ? languageValue : (String) valueMap.get("default"));
            }
        }
        return fieldValues;
    }

    public static String getFieldValue(String field, Map<String, Object> source) {
        final Object o = source.get(field);
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof HashMap && field.endsWith("Object")) {
            return (String) ((HashMap) o).get("default");
        }
        return null;
    }

    /**
     * Analyze a field and a value against the index
     * or query phase and return the first value generated
     * by the specified analyzer. For now mainly used for
     * synonyms analysis.
     * <p>
     * See https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-analyze.html
     * {
     * "tokens" : [
     * {
     * "token" : "area management/restriction/regulation zones and reporting units",
     * "start_offset" : 0,
     * "end_offset" : 64,
     * "type" : "word",
     * "position" : 0
     * }
     * ]
     * }
     * or when a synonym is found
     * {
     * "tokens" : [
     * {
     * "token" : "elevation",
     * "start_offset" : 0,
     * "end_offset" : 8,
     * "type" : "SYNONYM",
     * "position" : 0
     * }
     * ]
     * }
     *
     * @param fieldValue The field value to analyze
     * @return The analyzed string value if found or empty text if not found or if an exception occurred.
     */
    public static String analyzeField(String collection,
                                      String analyzer,
                                      String fieldValue) {

        AnalyzeRequest analyzeRequest = AnalyzeRequest.of(
            b -> b.index(collection)
                .analyzer(analyzer)
                // Replace , as it is meaningful in synonym map format
                .text(fieldValue.replace(",", ""))
        );

        try {
            AnalyzeResponse response = EsRestClient.get().client.indices().analyze(analyzeRequest);

            final List<AnalyzeToken> tokens = response.tokens();
            if (tokens.size() == 1) {
                final String type = tokens.get(0).type();
                if ("SYNONYM".equals(type) || "word".equals(type)) {
                    return tokens.get(0).token();
                }
                return "";
            } else {
                return "";
            }
        } catch (Exception ex) {
            return "";
        }
    }

    // TODO: check index exist too
    public String getServerStatus() throws IOException {

        HealthResponse response = client.cluster().health();
        return response.status().toString();
    }

    public String getServerVersion() throws IOException, ElasticsearchException {
        ElasticsearchVersionInfo version = client.info().version();

        return version.number();
    }
}
