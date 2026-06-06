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

package org.fao.geonet.api.es.processors.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class EsQueryFilterBuilder {
    private static final String QUERY = "query";
    private static final String BOOL = "bool";
    private static final String FILTER = "filter";
    private static final String MUST = "must";
    private static final String MATCH_ALL = "match_all";
    private static final String FUNCTION_SCORE = "function_score";
    private static final String GLOBAL = "global";
    private static final String AGGS = "aggs";
    private static final String AGGREGATIONS = "aggregations";

    /**
     * Privileges filter only allows
     * * op0 (ie. view operation) contains one of the ids of your groups
     */
    private static final String FILTER_TEMPLATE = " {\n" +
        "       \t\"query_string\": {\n" +
        "       \t\t\"query\": \"%s\"\n" +
        "       \t}\n" +
        "}";


    @Autowired
    NodeInfo node;

    public void addFilterToQuery(ServiceContext context,
                                  ObjectMapper objectMapper,
                                  JsonNode esQuery) throws Exception {

        // Build filter node
        boolean isSearchingForDraft = isSearchingForDraft(esQuery);
        String esFilter = buildQueryFilter(context, "", isSearchingForDraft);
        JsonNode nodeFilter = objectMapper.readTree(esFilter);

        JsonNode queryNode = esQuery.get(QUERY);

        // Replace any "global" aggregation with a "filter" aggregation scoped to
        // the ACL filter.
        // Must run after nodeFilter is built, before any branch exits early via return.
        for (String aggsKey : new String[]{AGGS, AGGREGATIONS}) {
            JsonNode aggsNode = esQuery.get(aggsKey);
            if (aggsNode != null && aggsNode.isObject()) {
                replaceGlobalAggregations((ObjectNode) aggsNode, nodeFilter);
            }
        }

        // Defensive: if no "query", create a bool { must: match_all, filter: nodeFilter }
        if (queryNode == null || queryNode.isNull()
            || (queryNode.isObject() && queryNode.isEmpty())) {
            ObjectNode boolNode = objectMapper.createObjectNode();
            // prefer must = match_all object (same shape as existing code)
            ObjectNode matchAll = objectMapper.createObjectNode();
            matchAll.putObject(MATCH_ALL);
            boolNode.set(MUST, matchAll);
            boolNode.set(FILTER, nodeFilter);
            ((ObjectNode) esQuery).set(QUERY, objectMapper.createObjectNode().set(BOOL, boolNode));
            return;
        }

        // Try to find the boolean node where to insert the filter.
        // Prefer function_score.query.bool if present because function_score
        // needs the filter to be applied to the inner query used for scoring.
        JsonNode functionScoreNode = queryNode.get(FUNCTION_SCORE);
        if (functionScoreNode != null && functionScoreNode.isObject()) {
            JsonNode innerQuery = functionScoreNode.get(QUERY);
            if (innerQuery == null || innerQuery.isNull()) {
                // create function_score.query.bool with only the filter
                ObjectNode boolNode = objectMapper.createObjectNode();
                boolNode.set(FILTER, nodeFilter);
                ((ObjectNode) functionScoreNode).set(QUERY, objectMapper.createObjectNode().set(BOOL, boolNode));
                return;
            }

            JsonNode innerBool = innerQuery.get(BOOL);
            if (innerBool != null && innerBool.isObject()) {
                insertFilter((ObjectNode) innerBool, nodeFilter);
                return;
            }

            // innerQuery exists but isn't a bool -> wrap it into a bool { must: <old>, filter: <new> }
            ObjectNode newBool = objectMapper.createObjectNode();
            newBool.set(MUST, innerQuery.deepCopy());
            newBool.set(FILTER, nodeFilter);
            ((ObjectNode) functionScoreNode).set(QUERY, objectMapper.createObjectNode().set(BOOL, newBool));
            return;
        }

        // Top-level bool
        JsonNode boolNode = queryNode.get(BOOL);
        if (boolNode != null && boolNode.isObject()) {
            insertFilter((ObjectNode) boolNode, nodeFilter);
            return;
        }

        // Other query shapes: wrap existing query into a bool { must: <existing query>, filter: nodeFilter }
        ObjectNode copy = queryNode.deepCopy();
        ObjectNode objectNodeBool = objectMapper.createObjectNode();
        objectNodeBool.set(MUST, copy);
        objectNodeBool.set(FILTER, nodeFilter);

        // Replace the existing "query" content with the new bool
        ((ObjectNode) queryNode).removeAll();
        ((ObjectNode) queryNode).set(BOOL, objectNodeBool);
    }

    private boolean isSearchingForDraft(JsonNode node) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (fieldName.equals("draft")) {
                    return true;
                }
                if (isSearchingForDraft(node.get(fieldName))) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode entry : node) {
                if (isSearchingForDraft(entry)) {
                    return true;
                }
            }
        } else if (node.isTextual()) {
            String text = node.asText();
            return text.contains("\"draft\":") || text.contains("+draft:") || text.contains("-draft:");
        }
        return false;
    }

    private void replaceGlobalAggregations(ObjectNode aggsNode, JsonNode aclFilter) {
        aggsNode.fields().forEachRemaining(entry -> {
            JsonNode aggDef = entry.getValue();
            if (!aggDef.isObject()) {
                return;
            }
            ObjectNode aggDefObj = (ObjectNode) aggDef;
            if (aggDefObj.has(GLOBAL)) {
                // "global" ignores the query scope; swap it for a filter-scoped bucket.
                aggDefObj.remove(GLOBAL);
                aggDefObj.set(FILTER, aclFilter);
            }
            // Recurse into nested sub-aggregations.
            for (String subKey : new String[]{AGGS, AGGREGATIONS}) {
                JsonNode sub = aggDefObj.get(subKey);
                if (sub != null && sub.isObject()) {
                    replaceGlobalAggregations((ObjectNode) sub, aclFilter);
                }
            }
        });
    }

    /**
     * Add search privilege criteria to a query.
     */
    private String buildQueryFilter(ServiceContext context, String type, boolean isSearchingForDraft) throws Exception {
        return String.format(FILTER_TEMPLATE,
            EsFilterBuilder.build(context, type, isSearchingForDraft, node));

    }

    private void insertFilter(ObjectNode objectNode, JsonNode nodeFilter) {
        JsonNode filter = objectNode.get(FILTER);
        if (filter == null || filter.isNull() || (filter.isObject() && filter.isEmpty())) {
            objectNode.set(FILTER, nodeFilter);
        } else if (filter.isArray()) {
            ((ArrayNode) filter).add(nodeFilter);
        } else {
            // existing filter is an object (or other non-array) -> convert to array preserving both
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            arr.add(filter);
            arr.add(nodeFilter);
            objectNode.set(FILTER, arr);
        }
    }
}
