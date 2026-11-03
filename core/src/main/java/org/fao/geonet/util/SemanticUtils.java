/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class SemanticUtils {
    @Value("${semantic.server.url:}")
    private String serverUrl;

    @Value("${semantic.server.model:}")
    private String serverModel;

    @Value("${semantic.server.model_type:}")
    private String serverModelType;

    @Value("${semantic.server.apikey:}")
    private String serverApiKey;

    public String buildEmbedding(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        if(StringUtils.isBlank(serverUrl)) {
            return "";
        }

        if(StringUtils.isBlank(serverModel)) {
            Log.error(Geonet.GEONETWORK, "When configuring a semantic server, you must provide a supported model. Check semantic.server.model in config.properties.");
            return "";
        }


        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestProperty("Content-Type", "application/json");
            if (StringUtils.isNotBlank(serverApiKey)) {
                connection.setRequestProperty("Authorization", "Bearer " + serverApiKey);
            }
            connection.setDoOutput(true);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", serverModel);
            payload.put("input", text);

            if(StringUtils.isNotBlank(serverModelType)) {
                payload.put("input_type", serverModelType);
            }

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(objectMapper.writeValueAsBytes(payload));
            }

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                InputStream errorStream = connection.getErrorStream();
                String error = errorStream != null ? IOUtils.toString(errorStream, StandardCharsets.UTF_8) : "";
                Log.error(Geonet.GEONETWORK, "Semantic embedding request failed with status " + status + ": " + error);
                return "";
            }

            try (InputStream inputStream = connection.getInputStream()) {
                JsonNode response = objectMapper.readTree(inputStream);
                JsonNode embedding = extractEmbedding(response);
                if (embedding == null) {
                    Log.error(Geonet.GEONETWORK,
                        "Semantic embedding response does not contain an embedding array.");
                    return "";
                }
                return objectMapper.writeValueAsString(embedding);
            }
        } catch (IOException e) {
            Log.error(Geonet.GEONETWORK, "Failed to build embedding: " + e.getMessage(), e);
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static JsonNode extractEmbedding(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }

        JsonNode embedding = response.get("embedding");
        if (embedding != null && embedding.isArray()) {
            return embedding;
        }

        JsonNode data = response.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                JsonNode itemEmbedding = item.get("embedding");
                if (itemEmbedding != null && itemEmbedding.isArray()) {
                    return itemEmbedding;
                }
            }
        }

        return null;
    }


    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerModel() {
        return serverModel;
    }

    public void setServerModel(String serverModel) {
        this.serverModel = serverModel;
    }

    public String getServerModelType() {
        return serverModelType;
    }

    public void setServerModelType(String serverModelType) {
        this.serverModelType = serverModelType;
    }

    public String getServerApiKey() {
        return serverApiKey;
    }

    public void setServerApiKey(String serverApiKey) {
        this.serverApiKey = serverApiKey;
    }

}
