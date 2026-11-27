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

package org.fao.geonet.api.es;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.XslUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class KnnUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    private KnnUtils() {
    }

    public static void replaceEmbeddingInKnnQuery(JsonNode node, ObjectMapper mapper) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode knnNode = objectNode.get("knn");
            if (knnNode != null && !updateKnnNode(knnNode, mapper)) {
                objectNode.remove("knn");
            }
            objectNode.fields().forEachRemaining(entry -> {
                if (!"knn".equals(entry.getKey())) {
                    replaceEmbeddingInKnnQuery(entry.getValue(), mapper);
                }
            });
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                replaceEmbeddingInKnnQuery(item, mapper);
            }
        }
    }

    public static void addFilterToKnnQuery(JsonNode node, JsonNode nodeFilter) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode knnNode = objectNode.get("knn");
            if (knnNode != null && !addFilterToKnnNode(knnNode, nodeFilter)) {
                objectNode.remove("knn");
            }

            objectNode.fields().forEachRemaining(entry -> {
                if (!"knn".equals(entry.getKey())) {
                    addFilterToKnnQuery(entry.getValue(), nodeFilter);
                }
            });
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                addFilterToKnnQuery(item, nodeFilter);
            }
        }
    }

    private static boolean updateKnnNode(JsonNode knnNode, ObjectMapper mapper) {
        if (knnNode.isObject()) {
            return replaceQueryVector((ObjectNode) knnNode, mapper);
        }
        if (knnNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) knnNode;
            boolean hasValidItem = false;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode knnItem = arrayNode.get(i);
                if (knnItem.isObject()) {
                    if (!replaceQueryVector((ObjectNode) knnItem, mapper)) {
                        arrayNode.remove(i);
                        i--;
                        continue;
                    }
                    hasValidItem = true;
                }
            }
            return hasValidItem;
        }
        return false;
    }

    private static boolean replaceQueryVector(ObjectNode knnObject, ObjectMapper mapper) {
        JsonNode queryVectorNode = knnObject.get("query_vector");
        if (queryVectorNode == null || !queryVectorNode.isTextual()) {
            return false;
        }

        String embeddingJson = XslUtil.buildEmbedding(queryVectorNode.asText());
        if (StringUtils.isBlank(embeddingJson)) {
            LOGGER.warn("Could not build embedding for knn query_vector.");
            return false;
        }

        try {
            JsonNode embeddingNode = mapper.readTree(embeddingJson);
            if (!embeddingNode.isArray()) {
                LOGGER.warn("Embedding payload is not an array: {}", embeddingNode.getNodeType());
                return false;
            }
            knnObject.set("query_vector", embeddingNode);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error parsing embedding payload.", e);
            return false;
        }
    }

    private static boolean addFilterToKnnNode(JsonNode knnNode, JsonNode nodeFilter) {
        if (knnNode.isObject()) {
            insertFilter((ObjectNode) knnNode, nodeFilter.deepCopy());
            return true;
        }

        if (knnNode.isArray()) {
            boolean hasItem = false;
            for (int i = 0; i < ((ArrayNode) knnNode).size(); i++) {
                JsonNode knnItem = ((ArrayNode) knnNode).get(i);
                if (knnItem.isObject()) {
                    insertFilter((ObjectNode) knnItem, nodeFilter.deepCopy());
                    hasItem = true;
                }
            }
            return hasItem;
        }

        return false;
    }

    private static void insertFilter(ObjectNode objectNode, JsonNode nodeFilter) {
        JsonNode filter = objectNode.get("filter");
        if (filter == null || filter.isNull() || (filter.isObject() && filter.isEmpty())) {
            objectNode.set("filter", nodeFilter);
        } else if (filter.isArray()) {
            ((ArrayNode) filter).add(nodeFilter);
        } else {
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            arr.add(filter);
            arr.add(nodeFilter);
            objectNode.set("filter", arr);
        }
    }
}
