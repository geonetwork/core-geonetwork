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
package org.fao.geonet.kernel.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class EsQueryFilterUtils {
    private EsQueryFilterUtils() {
    }

    public static JsonNode buildQueryStringFilter(ObjectMapper objectMapper, String query) {
        ObjectNode queryStringNode = objectMapper.createObjectNode();
        queryStringNode.put("query", query);
        ObjectNode nodeFilter = objectMapper.createObjectNode();
        nodeFilter.set("query_string", queryStringNode);
        return nodeFilter;
    }

    public static void addFilterToQuery(ObjectMapper objectMapper, JsonNode esQuery, JsonNode nodeFilter) {
        JsonNode queryNode = esQuery.get("query");

        // Replace any "global" aggregation with a "filter" aggregation scoped to
        // the ACL filter.
        // Must run after nodeFilter is built, before any branch exits early via return.
        for (String aggsKey : new String[]{"aggs", "aggregations"}) {
            JsonNode aggsNode = esQuery.get(aggsKey);
            if (aggsNode != null && aggsNode.isObject()) {
                replaceGlobalAggregations((ObjectNode) aggsNode, nodeFilter);
            }
        }

        // Defensive: if no "query", create a bool { must: match_all, filter: nodeFilter }
        if (queryNode == null || queryNode.isNull()
            || (queryNode.isObject() && queryNode.isEmpty())) {
            ObjectNode boolNode = objectMapper.createObjectNode();
            ObjectNode matchAll = objectMapper.createObjectNode();
            matchAll.putObject("match_all");
            boolNode.set("must", matchAll);
            boolNode.set("filter", nodeFilter);
            ((ObjectNode) esQuery).set("query", objectMapper.createObjectNode().set("bool", boolNode));
            return;
        }

        // Try to find the boolean node where to insert the filter.
        // Prefer function_score.query.bool if present because function_score
        // needs the filter to be applied to the inner query used for scoring.
        JsonNode functionScoreNode = queryNode.get("function_score");
        if (functionScoreNode != null && functionScoreNode.isObject()) {
            JsonNode innerQuery = functionScoreNode.get("query");
            if (innerQuery == null || innerQuery.isNull()) {
                ObjectNode boolNode = objectMapper.createObjectNode();
                boolNode.set("filter", nodeFilter);
                ((ObjectNode) functionScoreNode).set("query", objectMapper.createObjectNode().set("bool", boolNode));
                return;
            }

            JsonNode innerBool = innerQuery.get("bool");
            if (innerBool != null && innerBool.isObject()) {
                insertFilter((ObjectNode) innerBool, nodeFilter);
                return;
            }

            // innerQuery exists but isn't a bool -> wrap it into a bool { must: <old>, filter: <new> }
            ObjectNode newBool = objectMapper.createObjectNode();
            newBool.set("must", innerQuery.deepCopy());
            newBool.set("filter", nodeFilter);
            ((ObjectNode) functionScoreNode).set("query", objectMapper.createObjectNode().set("bool", newBool));
            return;
        }

        // Top-level bool
        JsonNode boolNode = queryNode.get("bool");
        if (boolNode != null && boolNode.isObject()) {
            insertFilter((ObjectNode) boolNode, nodeFilter);
            return;
        }

        // Other query shapes: wrap existing query into a bool { must: <existing query>, filter: nodeFilter }
        ObjectNode objectNodeBool = objectMapper.createObjectNode();
        objectNodeBool.set("must", queryNode.deepCopy());
        objectNodeBool.set("filter", nodeFilter);
        ((ObjectNode) esQuery).set("query", objectMapper.createObjectNode().set("bool", objectNodeBool));
    }

    private static void replaceGlobalAggregations(ObjectNode aggsNode, JsonNode aclFilter) {
        aggsNode.fields().forEachRemaining(entry -> {
            JsonNode aggDef = entry.getValue();
            if (!aggDef.isObject()) {
                return;
            }
            ObjectNode aggDefObj = (ObjectNode) aggDef;
            if (aggDefObj.has("global")) {
                // "global" ignores the query scope; swap it for a filter-scoped bucket.
                aggDefObj.remove("global");
                aggDefObj.set("filter", aclFilter);
            }
            // Recurse into nested sub-aggregations.
            for (String subKey : new String[]{"aggs", "aggregations"}) {
                JsonNode sub = aggDefObj.get(subKey);
                if (sub != null && sub.isObject()) {
                    replaceGlobalAggregations((ObjectNode) sub, aclFilter);
                }
            }
        });
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
