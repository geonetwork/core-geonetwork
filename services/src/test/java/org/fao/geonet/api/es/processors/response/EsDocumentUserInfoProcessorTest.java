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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class EsDocumentUserInfoProcessorTest {

    private EsDocumentUserInfoProcessor processor;
    private ObjectMapper mapper;

    @Mock
    private ServiceContext context;

    @Mock
    private AccessManager accessManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new EsDocumentUserInfoProcessor();
        mapper = new ObjectMapper();
        when(context.getBean(AccessManager.class)).thenReturn(accessManager);
    }

    @Test
    public void process_ShouldSetPermissions_WhenUserIsOwner() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.OWNER, 1);
        source.put(Geonet.IndexFieldNames.ID, "123");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(true);
        when(accessManager.hasReviewPermission(eq(context), eq("123"))).thenReturn(true);

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(Edit.Info.Elem.OWNER).asBoolean());
        assertTrue(doc.get(Edit.Info.Elem.EDIT).asBoolean());
        assertTrue(doc.get(Edit.Info.Elem.REVIEW).asBoolean());
        assertEquals(1, doc.get("ownerId").asInt());

        // Check reserved operations added to doc
        for (ReservedOperation op : ReservedOperation.values()) {
            if (op != ReservedOperation.editing) { // editing is not added via addReservedOperation in the original code
                assertTrue("Should have " + op.name(), doc.get(op.name()).asBoolean());
            }
        }
    }

    @Test
    public void process_ShouldSetPermissions_WhenUserIsNotOwnerButHasGroups() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.OWNER, 1);
        source.put(Geonet.IndexFieldNames.ID, "123");

        // Add operation permissions to source
        source.put("op" + ReservedOperation.view.getId(), 10); // Group 10 has view
        source.put("op" + ReservedOperation.editing.getId(), 20); // Group 20 has edit

        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(false);
        when(accessManager.getUserGroups(any(), any(), eq(false))).thenReturn(Collections.singleton(10));
        when(accessManager.getUserGroups(any(), any(), eq(true))).thenReturn(Collections.singleton(20));
        when(accessManager.hasReviewPermission(eq(context), eq("123"))).thenReturn(false);

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(Edit.Info.Elem.OWNER).asBoolean());
        assertTrue(doc.get(Edit.Info.Elem.EDIT).asBoolean()); // has editing group
        assertFalse(doc.get(Edit.Info.Elem.REVIEW).asBoolean());

        assertTrue(doc.get(ReservedOperation.view.name()).asBoolean());
        assertFalse(doc.get(ReservedOperation.download.name()).asBoolean());
    }

    @Test
    public void process_ShouldSetIsPublishedToAll_WhenAllGroupHasView() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put("op" + ReservedOperation.view.getId(), ReservedGroup.all.getId());
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(false);
        when(accessManager.getUserGroups(any(), any(), anyBoolean())).thenReturn(Collections.emptySet());

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(Edit.Info.Elem.IS_PUBLISHED_TO_ALL).asBoolean());
    }

    @Test
    public void process_ShouldSetGuestDownload_WhenGuestGroupHasDownload() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put("op" + ReservedOperation.download.getId(), ReservedGroup.guest.getId());
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(false);
        when(accessManager.getUserGroups(any(), any(), anyBoolean())).thenReturn(Collections.emptySet());

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(Edit.Info.Elem.GUEST_DOWNLOAD).asBoolean());
    }


    @Test
    public void process_ShouldHandleArrayOfGroups() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        ArrayNode viewGroups = mapper.createArrayNode();
        viewGroups.add(10);
        viewGroups.add(ReservedGroup.all.getId());
        source.set("op" + ReservedOperation.view.getId(), viewGroups);
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(false);
        when(accessManager.getUserGroups(any(), any(), eq(false))).thenReturn(Collections.singleton(10));
        when(accessManager.getUserGroups(any(), any(), eq(true))).thenReturn(Collections.emptySet());

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(ReservedOperation.view.name()).asBoolean());
        assertTrue(doc.get(Edit.Info.Elem.IS_PUBLISHED_TO_ALL).asBoolean());
    }

    @Test
    public void process_ShouldHandleMissingSourceNode() throws Exception {
        ObjectNode doc = mapper.createObjectNode();

        when(accessManager.isOwner(eq(context), any(MetadataSourceInfo.class))).thenReturn(false);
        when(accessManager.getUserGroups(any(), any(), anyBoolean())).thenReturn(Collections.emptySet());

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(Edit.Info.Elem.OWNER).asBoolean());
        assertFalse(doc.get(Edit.Info.Elem.EDIT).asBoolean());
        assertFalse(doc.get(Edit.Info.Elem.IS_PUBLISHED_TO_ALL).asBoolean());
    }
}
