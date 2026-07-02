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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.EsSearchEndpoints;
import org.fao.geonet.api.es.JsonStreamUtils;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SelectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Processes the Elasticsearch response to filter and add additional information.
 */
@Component
public class EsResponseProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

    @Autowired
    private EsDocumentSelectionInfoProcessor esDocumentSelectionInfoProcessor;

    @Autowired
    private EsDocumentUserInfoProcessor esDocumentUserInfoProcessor;

    @Autowired
    private EsDocumentRelatedTypesProcessor esDocumentRelatedTypesProcessor;

    @Autowired
    private EsDocumentMetadataFiltersProcessor esDocumentMetadataFiltersProcessor;

    @Autowired
    private EsDocumentRemovePrivilegesProcessor esDocumentRemovePrivilegesProcessor;

    public void processResponse(ServiceContext context, HttpSession httpSession,
                                 InputStream streamFromServer, OutputStream streamToClient,
                                 String endPoint,
                                 String bucket,
                                 boolean addPermissions,
                                 RelatedItemType[] relatedTypes) throws Exception {
        JsonParser parser = JsonStreamUtils.jsonFactory.createParser(streamFromServer);
        JsonGenerator generator = JsonStreamUtils.jsonFactory.createGenerator(streamToClient);
        parser.nextToken();  //Go to the first token

        final Set<String> selections = (addPermissions ?
            SelectionManager.getManager(ApiUtils.getUserSession(httpSession)).getSelection(bucket) : new HashSet<>());

        Map<String, Object> processorParams = new HashMap<>();
        processorParams.put("selections", selections);
        processorParams.put("relatedTypes", relatedTypes);

        if (endPoint.equals(EsSearchEndpoints.SEARCH_ENDPOINT.toString())) {
            JsonStreamUtils.addInfoToDocs(parser, generator, doc -> {
                try {
                    if (addPermissions) {
                        esDocumentUserInfoProcessor.process(doc, context, processorParams);
                        esDocumentSelectionInfoProcessor.process(doc, context, processorParams);
                    }

                    esDocumentRelatedTypesProcessor.process(doc, context, processorParams);

                    ObjectNode sourceNode = ObjectNodeUtils.getSourceNode(doc);
                    if (sourceNode != null) {
                        if (sourceNode.has(Geonet.IndexFieldNames.SCHEMA)) {
                            try {
                                // Apply metadata schema filters to remove non-allowed fields
                                esDocumentMetadataFiltersProcessor.process(doc, context, processorParams);
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Failed to load metadata schema for {}. Error is: {}",
                                    ObjectNodeUtils.getSourceString(doc, Geonet.IndexFieldNames.UUID),
                                    e.getMessage()
                                );
                            }
                        }

                        // Remove fields with privileges info
                        esDocumentRemovePrivilegesProcessor.process(doc, context, processorParams);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error processing document: {}", e.getMessage(), e);
                }
            });
        } else {
            JsonStreamUtils.addInfoToDocsMSearch(parser, generator, doc -> {
                try {
                    if (addPermissions) {
                        esDocumentUserInfoProcessor.process(doc, context, processorParams);
                        esDocumentSelectionInfoProcessor.process(doc, context, processorParams);
                    }

                    esDocumentRelatedTypesProcessor.process(doc, context, processorParams);

                    // Remove fields with privileges info
                    esDocumentRemovePrivilegesProcessor.process(doc, context, processorParams);
                } catch (Exception e) {
                    LOGGER.error("Error processing document: {}", e.getMessage(), e);
                }
            });
        }

        generator.flush();
        generator.close();
    }
}
