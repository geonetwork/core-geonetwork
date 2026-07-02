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
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataOperationFilterType;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EsDocumentMetadataFiltersProcessorTest {

    @InjectMocks
    private EsDocumentMetadataFiltersProcessor processor;

    @Mock
    private SchemaManager schemaManager;

    @Mock
    private ServiceContext context;

    @Mock
    private UserSession userSession;

    @Mock
    private AccessManager accessManager;

    @Mock
    private MetadataSchema metadataSchema;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(context.getUserSession()).thenReturn(userSession);
        when(context.getBean(AccessManager.class)).thenReturn(accessManager);
    }

    @Test
    public void processMetadataSchemaFilters_ShouldReturn_WhenSourceNodeIsMissing() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        processor.process(doc, context, Collections.emptyMap());
        assertFalse(doc.has(ObjectNodeUtils.SOURCE_NODE));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldReturn_WhenSchemaIsInvalid() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "invalid-schema");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(schemaManager.getSchema("invalid-schema")).thenThrow(new IllegalArgumentException("Invalid schema"));

        processor.process(doc, context, Collections.emptyMap());

        // Source node should remain unchanged
        assertEquals("invalid-schema", doc.get(ObjectNodeUtils.SOURCE_NODE).get(Geonet.IndexFieldNames.SCHEMA).asText());
    }

    @Test
    public void processMetadataSchemaFilters_ShouldApplyAuthenticatedFilter_WhenNotAuthenticated() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put("secretField", "secretValue");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(false);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.secretField");
        when(metadataSchema.getOperationFilter(MetadataOperationFilterType.authenticated.name())).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(ObjectNodeUtils.SOURCE_NODE).has("secretField"));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldNotApplyAuthenticatedFilter_WhenAuthenticated() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put("secretField", "secretValue");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.RegisteredUser);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.secretField");
        when(metadataSchema.getOperationFilter(MetadataOperationFilterType.authenticated.name())).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(ObjectNodeUtils.SOURCE_NODE).has("secretField"));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldApplyEditFilter_WhenCannotEdit() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put("editOnlyField", "value");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        doc.put("edit", false);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.Administrator);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.editOnlyField");
        when(metadataSchema.getOperationFilter(ReservedOperation.editing)).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(ObjectNodeUtils.SOURCE_NODE).has("editOnlyField"));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldHandlePathNotFoundExceptionGracefully() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        doc.put("edit", false);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.Administrator);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.nonExistentField");
        when(metadataSchema.getOperationFilter(ReservedOperation.editing)).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        // Should not throw exception and source should be same as before (modulo formatting)
        assertTrue(doc.has(ObjectNodeUtils.SOURCE_NODE));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldApplyGroupOwnerFilter_WhenNotGroupOwner() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 10);
        source.put("ownerOnlyField", "value");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.Editor);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.ownerOnlyField");
        when(metadataSchema.getOperationFilter(MetadataOperationFilterType.groupOwner.name())).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(ObjectNodeUtils.SOURCE_NODE).has("ownerOnlyField"));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldNotApplyGroupOwnerFilter_WhenIsAdministrator() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 10);
        source.put("ownerOnlyField", "value");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.Administrator);

        MetadataSchemaOperationFilter filter = mock(MetadataSchemaOperationFilter.class);
        when(filter.getJsonpath()).thenReturn("$.ownerOnlyField");
        when(metadataSchema.getOperationFilter(MetadataOperationFilterType.groupOwner.name())).thenReturn(filter);

        processor.process(doc, context, Collections.emptyMap());

        assertTrue(doc.get(ObjectNodeUtils.SOURCE_NODE).has("ownerOnlyField"));
    }

    @Test
    public void processMetadataSchemaFilters_ShouldApplyMultipleFilters() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.SCHEMA, "iso19139");
        source.put("field1", "value1");
        source.put("field2", "value2");
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        doc.put("edit", false);
        doc.put("download", false);

        when(schemaManager.getSchema("iso19139")).thenReturn(metadataSchema);
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getProfile()).thenReturn(Profile.Administrator);

        MetadataSchemaOperationFilter editFilter = mock(MetadataSchemaOperationFilter.class);
        when(editFilter.getJsonpath()).thenReturn("$.field1");
        when(metadataSchema.getOperationFilter(ReservedOperation.editing)).thenReturn(editFilter);

        MetadataSchemaOperationFilter downloadFilter = mock(MetadataSchemaOperationFilter.class);
        when(downloadFilter.getJsonpath()).thenReturn("$.field2");
        when(metadataSchema.getOperationFilter(ReservedOperation.download)).thenReturn(downloadFilter);

        processor.process(doc, context, Collections.emptyMap());

        assertFalse(doc.get(ObjectNodeUtils.SOURCE_NODE).has("field1"));
        assertFalse(doc.get(ObjectNodeUtils.SOURCE_NODE).has("field2"));
    }
}
