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
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Geonet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EsQueryProcessor {
    @Value("${es.index.records:gn-records}")
    private String defaultIndex;

    @Autowired
    private EsQueryFilterBuilder queryFilterBuilder;

    public String process(ServiceContext context, String body, String selectionBucket) throws Exception {
        UserSession session = context.getUserSession();
        ObjectMapper objectMapper = new ObjectMapper();

        // multisearch support
        final MappingIterator<Object> mappingIterator = objectMapper.readerFor(JsonNode.class).readValues(body);
        StringBuffer requestBody = new StringBuffer();
        while (mappingIterator.hasNextValue()) {
            JsonNode node = (JsonNode) mappingIterator.nextValue();
            final JsonNode indexNode = node.get("index");
            if (indexNode != null) {
                ((ObjectNode) node).put("index", defaultIndex);
            } else {
                queryFilterBuilder.addFilterToQuery(context, objectMapper, node);
                if (selectionBucket != null) {
                    // Multisearch are not supposed to work with a bucket.
                    // Only one request is store in session
                    session.setProperty(Geonet.Session.SEARCH_REQUEST + selectionBucket, node);
                }
                final JsonNode sourceNode = node.get(ObjectNodeUtils.SOURCE_NODE);
                if (sourceNode != null) {
                    if (sourceNode.isArray()) {
                        addRequiredField((ArrayNode) sourceNode);
                    } else {
                        final JsonNode sourceIncludes = sourceNode.get("includes");
                        if (sourceIncludes != null && sourceIncludes.isArray()) {
                            addRequiredField((ArrayNode) sourceIncludes);
                        }
                    }
                }
            }
            requestBody.append(node).append(System.lineSeparator());
        }

        return requestBody.toString();
    }

    private void addRequiredField(ArrayNode source) {
        source.add("op*");
        source.add(Geonet.IndexFieldNames.SCHEMA);
        source.add(Geonet.IndexFieldNames.GROUP_OWNER);
        source.add(Geonet.IndexFieldNames.OWNER);
        source.add(Geonet.IndexFieldNames.ID);
    }
}
