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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.api.records.MetadataUtils;
import org.fao.geonet.api.records.model.related.AssociatedRecord;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Processes an Elasticsearch response document to add related metadata information.
 */
@Component
public class EsDocumentRelatedTypesProcessor implements EsDocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(ObjectNode doc, ServiceContext context, Map<String, Object> parameters) throws Exception {
        RelatedItemType[] relatedTypes = (RelatedItemType[]) parameters.get("relatedTypes");

        if (relatedTypes != null && relatedTypes.length > 0) {
            addRelatedTypes(doc, relatedTypes, context);
        }
    }

    private void addRelatedTypes(ObjectNode doc,
                                        RelatedItemType[] relatedTypes,
                                        ServiceContext context) {
        Map<RelatedItemType, List<AssociatedRecord>> related = null;
        try {
            if (doc.has(ObjectNodeUtils.SOURCE_NODE) && doc.get(ObjectNodeUtils.SOURCE_NODE).has("id")) {
                related = MetadataUtils.getAssociated(
                    context,
                    context.getBean(IMetadataUtils.class)
                        .findOne(doc.get(ObjectNodeUtils.SOURCE_NODE).get("id").asText()),
                    relatedTypes, 0, 1000);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load related types for {}. Error is: {}",
                ObjectNodeUtils.getSourceString(doc, Geonet.IndexFieldNames.UUID),
                e.getMessage()
            );
        }
        doc.set("related", mapper.valueToTree(related));
    }
}
