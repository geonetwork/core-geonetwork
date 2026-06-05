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
    @Value("${semantic.server.url:http://localhost:11434/api/embeddings}")
    private String semanticServerUrl;

    @Value("${semantic.server.model:bge-m3}")
    private String semanticServerModel;

    public String buildEmbedding(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(semanticServerUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", semanticServerModel);
            payload.put("prompt", text);

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
                JsonNode embedding = response.get("embedding");
                if (embedding == null || !embedding.isArray()) {
                    Log.error(Geonet.GEONETWORK, "Semantic embedding response does not contain an embedding array.");
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
}
