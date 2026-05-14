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

package org.fao.geonet.api.es.processors.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataOperationFilterType;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Process an Elasticsearch document to filter out the elements defined in the metadata schema filters.
 *
 * It uses a jsonpath to filter the elements, typically is configured with the following jsonpath, to
 * filter the ES object elements with an attribute nilReason = 'withheld'.
 *
 *  $.*[?(@.nilReason == 'withheld')]
 *
 * The metadata index process, has to define this attribute. Any element that requires to be filtered, should be
 * defined as an object in Elasticsearch.
 *
 * Example for contacts:
 *
 *  <xsl:template mode="index-contact" match="*[cit:CI_Responsibility]">
 *      ...
 *      <!-- Check if the contact has an attribute @gco:nilReason = 'withheld', added by update-fixed-info.xsl process -->
 *      <xsl:variable name="hasWithheld" select="@gco:nilReason = 'withheld'" as="xs:boolean" />
 *
 *      <xsl:element name="contact{$fieldSuffix}">
 *        <xsl:attribute name="type" select="'object'"/>{
 *        ...
 *        "address":"<xsl:value-of select="util:escapeForJson($address)"/>"
 *        <xsl:if test="$hasWithheld">
 *         ,"nilReason": "withheld"
 *        </xsl:if>
 */
@Component
public class EsDocumentMetadataFiltersProcessor implements EsDocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    @Autowired
    private SchemaManager schemaManager;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Configuration configuration = Configuration.builder()
        .jsonProvider(new JacksonJsonNodeJsonProvider(mapper))
        .mappingProvider(new JacksonMappingProvider(mapper))
        .build();

    @Override
    public void process(ObjectNode doc, ServiceContext context, Map<String, Object> parameters) throws Exception {
        processMetadataSchemaFilters(context, doc);
    }

    private void processMetadataSchemaFilters(ServiceContext context, ObjectNode doc) throws Exception {
        ObjectNode sourceNode = ObjectNodeUtils.getSourceNode(doc);
        if (sourceNode == null) {
            return;
        }

        MetadataSchema mds;

        try {
            String metadataSchema = sourceNode.get(Geonet.IndexFieldNames.SCHEMA).asText();
            mds = schemaManager.getSchema(metadataSchema);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Failed to load metadata schema for {}. Error is: {}",
                ObjectNodeUtils.getSourceString(doc, Geonet.IndexFieldNames.UUID),
                e.getMessage()
            );

            return;
        }

        MetadataSchemaOperationFilter authenticatedFilter = mds.getOperationFilter(MetadataOperationFilterType.authenticated.name());

        List<String> jsonpathFilters = new ArrayList<>();

        if (authenticatedFilter != null && !context.getUserSession().isAuthenticated()) {
            jsonpathFilters.add(authenticatedFilter.getJsonpath());
        }
        //do the same for groupOwner
        MetadataSchemaOperationFilter groupOwnerFilter = mds.getOperationFilter(MetadataOperationFilterType.groupOwner.name());

        if (groupOwnerFilter != null) {
            if (context.getUserSession().getProfile() != Profile.Administrator) {
                final AccessManager accessManager = context.getBean(AccessManager.class);
                Collection<Integer> userGroups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), true);
                Integer groupOwner = ObjectNodeUtils.getSourceInteger(doc, Geonet.IndexFieldNames.GROUP_OWNER);
                boolean isGroupOwner = groupOwner != null && userGroups.contains(groupOwner);

                if (!isGroupOwner) {
                    jsonpathFilters.add(groupOwnerFilter.getJsonpath());
                }
            }
        }

        MetadataSchemaOperationFilter editFilter = mds.getOperationFilter(ReservedOperation.editing);

        if (editFilter != null) {
            boolean canEdit = doc.get("edit").asBoolean();

            if (!canEdit) {
                jsonpathFilters.add(editFilter.getJsonpath());
            }
        }

        MetadataSchemaOperationFilter downloadFilter = mds.getOperationFilter(ReservedOperation.download);
        if (downloadFilter != null) {
            boolean canDownload = doc.get("download").asBoolean();

            if (!canDownload) {
                jsonpathFilters.add(downloadFilter.getJsonpath());
            }
        }

        MetadataSchemaOperationFilter dynamicFilter = mds.getOperationFilter(ReservedOperation.dynamic);
        if (dynamicFilter != null) {
            boolean canDynamic = doc.get("dynamic").asBoolean();

            if (!canDynamic) {
                jsonpathFilters.add(dynamicFilter.getJsonpath());
            }
        }

        JsonNode actualObj = filterResponseElements(mapper, sourceNode, jsonpathFilters);
        if (actualObj != null) {
            doc.set(ObjectNodeUtils.SOURCE_NODE, actualObj);
        }
    }

    private JsonNode filterResponseElements(ObjectMapper mapper, ObjectNode sourceNode, List<String> jsonPathFilters) throws JsonProcessingException {
        DocumentContext jsonContext = JsonPath.using(configuration).parse(sourceNode);

        for(String jsonPath : jsonPathFilters) {
            if (StringUtils.isNotBlank(jsonPath)) {
                try {
                    jsonContext = jsonContext.delete(jsonPath);
                } catch (PathNotFoundException ex) {
                    // The node to remove is not returned in the response, ignore the error
                }
            }
        }

        return jsonContext.json();
    }
}
