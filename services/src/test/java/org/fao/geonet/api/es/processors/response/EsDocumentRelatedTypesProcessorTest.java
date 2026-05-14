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
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EsDocumentRelatedTypesProcessorTest {

    private EsDocumentRelatedTypesProcessor processor;
    private ObjectMapper mapper;

    @Mock
    private ServiceContext context;

    @Mock
    private IMetadataUtils metadataUtils;

    @Mock
    private AbstractMetadata metadata;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new EsDocumentRelatedTypesProcessor();
        mapper = new ObjectMapper();
        when(context.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
    }

    @Test
    public void process_ShouldAddRelatedInfo_WhenSuccessful() throws Exception {
        String docId = "123";
        ObjectNode doc = createDocWithId(docId);
        RelatedItemType[] types = new RelatedItemType[]{RelatedItemType.parent};

        when(metadataUtils.findOne(docId)).thenReturn(metadata);

        Map<RelatedItemType, List<AssociatedRecord>> associated = new HashMap<>();
        List<AssociatedRecord> records = new ArrayList<>();
        AssociatedRecord record = new AssociatedRecord();
        record.setUuid("related-uuid");
        records.add(record);
        associated.put(RelatedItemType.parent, records);

        try (MockedStatic<MetadataUtils> mockedMetadataUtils = mockStatic(MetadataUtils.class)) {
            mockedMetadataUtils.when(() -> MetadataUtils.getAssociated(
                eq(context), eq(metadata), eq(types), anyInt(), anyInt()
            )).thenReturn(associated);

            Map<String, Object> params = new HashMap<>();
            params.put("relatedTypes", types);
            processor.process(doc, context, params);

            assertTrue(doc.has("related"));
            assertNotNull(doc.get("related").get("parent"));
        }
    }

    @Test
    public void process_ShouldSetRelatedToNull_WhenExceptionOccurs() throws Exception {
        String docId = "123";
        ObjectNode doc = createDocWithId(docId);
        RelatedItemType[] types = new RelatedItemType[]{RelatedItemType.parent};

        when(metadataUtils.findOne(docId)).thenReturn(metadata);

        try (MockedStatic<MetadataUtils> mockedMetadataUtils = mockStatic(MetadataUtils.class)) {
            mockedMetadataUtils.when(() -> MetadataUtils.getAssociated(
                any(), any(), any(), anyInt(), anyInt()
            )).thenThrow(new RuntimeException("Test exception"));

            Map<String, Object> params = new HashMap<>();
            params.put("relatedTypes", types);
            processor.process(doc, context, params);

            assertTrue(doc.has("related"));
            assertTrue(doc.get("related").isNull());
        }
    }

    @Test
    public void process_ShouldHandleMissingId() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        RelatedItemType[] types = new RelatedItemType[]{RelatedItemType.parent};

        Map<String, Object> params = new HashMap<>();
        params.put("relatedTypes", types);
        processor.process(doc, context, params);

        assertTrue(doc.has("related"));
        assertTrue(doc.get("related").isNull());
    }

    @Test
    public void process_ShouldHandleMissingSource() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        RelatedItemType[] types = new RelatedItemType[]{RelatedItemType.parent};

        Map<String, Object> params = new HashMap<>();
        params.put("relatedTypes", types);
        processor.process(doc, context, params);

        assertTrue(doc.has("related"));
        assertTrue(doc.get("related").isNull());
    }

    private ObjectNode createDocWithId(String id) {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.ID, id);
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        return doc;
    }
}
