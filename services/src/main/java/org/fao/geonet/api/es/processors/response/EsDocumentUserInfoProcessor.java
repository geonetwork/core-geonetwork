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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Processes an Elasticsearch response document to add user and privileges information.
 */
@Component
public class EsDocumentUserInfoProcessor implements EsDocumentProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(ObjectNode doc, ServiceContext context, Map<String, Object> parameters) throws Exception {
        addUserInfo(doc, context);
    }

    private void addUserInfo(ObjectNode doc, ServiceContext context) throws Exception {
        final Integer owner = ObjectNodeUtils.getSourceInteger(doc, Geonet.IndexFieldNames.OWNER);
        final Integer groupOwner = ObjectNodeUtils.getSourceInteger(doc, Geonet.IndexFieldNames.GROUP_OWNER);
        final String id = ObjectNodeUtils.getSourceString(doc, Geonet.IndexFieldNames.ID);

        final MetadataSourceInfo sourceInfo = new MetadataSourceInfo();
        sourceInfo.setOwner(owner);
        if (groupOwner != null) {
            sourceInfo.setGroupOwner(groupOwner);
        }
        final AccessManager accessManager = context.getBean(AccessManager.class);
        final boolean isOwner = accessManager.isOwner(context, sourceInfo);
        final HashSet<ReservedOperation> operations;
        boolean canEdit = false;
        if (isOwner) {
            operations = Sets.newHashSet(Arrays.asList(ReservedOperation.values()));
            if (owner != null) {
                doc.put("ownerId", owner.intValue());
            }
        } else {
            final Collection<Integer> groups =
                accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
            final Collection<Integer> editingGroups =
                accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), true);
            operations = Sets.newHashSet();
            ObjectNode sourceNode = ObjectNodeUtils.getSourceNode(doc);
            if (sourceNode != null) {
                for (ReservedOperation operation : ReservedOperation.values()) {
                    final JsonNode operationNodes = sourceNode.get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
                    if (operationNodes != null) {
                        ArrayNode opFields = operationNodes.isArray() ? (ArrayNode) operationNodes : objectMapper.createArrayNode().add(operationNodes);
                        if (opFields != null) {
                            for (JsonNode field : opFields) {
                                final int groupId = field.asInt();
                                if (operation == ReservedOperation.editing
                                    && !canEdit
                                    && editingGroups.contains(groupId)) {
                                    canEdit = true;
                                }

                                if (groups.contains(groupId)) {
                                    operations.add(operation);
                                }
                            }
                        }
                    }
                }
            }
        }
        doc.put(Edit.Info.Elem.EDIT, isOwner || canEdit);
        doc.put(Edit.Info.Elem.REVIEW,
            id != null && accessManager.hasReviewPermission(context, id));
        doc.put(Edit.Info.Elem.OWNER, isOwner);
        doc.put(Edit.Info.Elem.IS_PUBLISHED_TO_ALL, hasOperation(doc, ReservedGroup.all, ReservedOperation.view));
        addReservedOperation(doc, operations, ReservedOperation.view);
        addReservedOperation(doc, operations, ReservedOperation.notify);
        addReservedOperation(doc, operations, ReservedOperation.download);
        addReservedOperation(doc, operations, ReservedOperation.dynamic);
        addReservedOperation(doc, operations, ReservedOperation.featured);

        if (!operations.contains(ReservedOperation.download)) {
            doc.put(Edit.Info.Elem.GUEST_DOWNLOAD, hasOperation(doc, ReservedGroup.guest, ReservedOperation.download));
        }
    }

    private void addReservedOperation(ObjectNode doc, HashSet<ReservedOperation> operations,
                                             ReservedOperation kind) {
        doc.put(kind.name(), operations.contains(kind));
    }

    private boolean hasOperation(ObjectNode doc, ReservedGroup group, ReservedOperation operation) {
        ObjectMapper objectMapper = new ObjectMapper();
        int groupId = group.getId();
        ObjectNode sourceNode = ObjectNodeUtils.getSourceNode(doc);
        if (sourceNode != null) {
            final JsonNode operationNodes = sourceNode.get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
            if (operationNodes != null) {
                ArrayNode opFields = operationNodes.isArray() ? (ArrayNode) operationNodes : objectMapper.createArrayNode().add(operationNodes);
                if (opFields != null) {
                    for (JsonNode field : opFields) {
                        if (groupId == field.asInt()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
