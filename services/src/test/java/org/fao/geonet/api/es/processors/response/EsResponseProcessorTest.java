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
import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.EsSearchEndpoints;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.kernel.SelectionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EsResponseProcessorTest {

    @InjectMocks
    private EsResponseProcessor esResponseProcessor;

    @Mock
    private EsDocumentSelectionInfoProcessor esDocumentSelectionInfoProcessor;

    @Mock
    private EsDocumentUserInfoProcessor esDocumentUserInfoProcessor;

    @Mock
    private EsDocumentRelatedTypesProcessor esDocumentRelatedTypesProcessor;

    @Mock
    private EsDocumentMetadataFiltersProcessor esDocumentMetadataFiltersProcessor;

    @Mock
    private EsDocumentRemovePrivilegesProcessor esDocumentRemovePrivilegesProcessor;

    @Mock
    private ServiceContext context;

    @Mock
    private HttpSession httpSession;

    @Mock
    private UserSession userSession;

    @Mock
    private SelectionManager selectionManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpSession.getAttribute(Jeeves.Elem.SESSION)).thenReturn(userSession);
    }

    @Test
    public void testProcessResponse_Search() throws Exception {
        String jsonResponse = "{\"hits\":{\"hits\":[{\"_source\":{\"uuid\":\"uuid1\",\"documentStandard\":\"iso19139\"}}]}}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (MockedStatic<SelectionManager> selectionManagerMockedStatic = mockStatic(SelectionManager.class);
             MockedStatic<ApiUtils> apiUtilsMockedStatic = mockStatic(ApiUtils.class)) {
            apiUtilsMockedStatic.when(() -> ApiUtils.getUserSession(httpSession)).thenReturn(userSession);
            selectionManagerMockedStatic.when(() -> SelectionManager.getManager(userSession)).thenReturn(selectionManager);
            when(selectionManager.getSelection(any())).thenReturn(new HashSet<>(Collections.singletonList("uuid1")));

            esResponseProcessor.processResponse(context, httpSession, inputStream, outputStream,
                EsSearchEndpoints.SEARCH_ENDPOINT.toString(), "metadata", true, null);

            verify(esDocumentUserInfoProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentSelectionInfoProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentMetadataFiltersProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentRemovePrivilegesProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
        }

        String result = outputStream.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("uuid1"));
    }

    @Test
    public void testProcessResponse_MSearch() throws Exception {
        String jsonResponse = "{\"responses\":[{\"hits\":{\"hits\":[{\"_source\":{\"uuid\":\"uuid1\",\"documentStandard\":\"iso19139\"}}]}}]}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (MockedStatic<SelectionManager> selectionManagerMockedStatic = mockStatic(SelectionManager.class);
             MockedStatic<ApiUtils> apiUtilsMockedStatic = mockStatic(ApiUtils.class)) {
            apiUtilsMockedStatic.when(() -> ApiUtils.getUserSession(httpSession)).thenReturn(userSession);
            selectionManagerMockedStatic.when(() -> SelectionManager.getManager(userSession)).thenReturn(selectionManager);
            when(selectionManager.getSelection(any())).thenReturn(new HashSet<>());

            esResponseProcessor.processResponse(context, httpSession, inputStream, outputStream,
                EsSearchEndpoints.MULTISEARCH_ENDPOINT.toString(), "metadata", true, null);

            verify(esDocumentUserInfoProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentSelectionInfoProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentRemovePrivilegesProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
            // Metadata filters are NOT applied in msearch based on the code logic (currently)
            verify(esDocumentMetadataFiltersProcessor, never()).process(any(), any(), any());
        }

        String result = outputStream.toString(StandardCharsets.UTF_8.name());
        assertTrue(result.contains("uuid1"));
    }

    @Test
    public void testProcessResponse_WithRelatedTypes() throws Exception {
        String jsonResponse = "{\"hits\":{\"hits\":[{\"_source\":{\"uuid\":\"uuid1\",\"documentStandard\":\"iso19139\"}}]}}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RelatedItemType[] relatedTypes = new RelatedItemType[]{RelatedItemType.associated};

        try (MockedStatic<SelectionManager> selectionManagerMockedStatic = mockStatic(SelectionManager.class);
             MockedStatic<ApiUtils> apiUtilsMockedStatic = mockStatic(ApiUtils.class)) {
            apiUtilsMockedStatic.when(() -> ApiUtils.getUserSession(httpSession)).thenReturn(userSession);
            selectionManagerMockedStatic.when(() -> SelectionManager.getManager(userSession)).thenReturn(selectionManager);
            when(selectionManager.getSelection(any())).thenReturn(new HashSet<>());

            esResponseProcessor.processResponse(context, httpSession, inputStream, outputStream,
                EsSearchEndpoints.SEARCH_ENDPOINT.toString(), "metadata", false, relatedTypes);

            verify(esDocumentRelatedTypesProcessor, times(1)).process(any(ObjectNode.class), eq(context), anyMap());
        }
    }

    @Test
    public void testProcessResponse_MetadataFiltersException() throws Exception {
        String jsonResponse = "{\"hits\":{\"hits\":[{\"_source\":{\"uuid\":\"uuid1\",\"documentStandard\":\"iso19139\"}}]}}";
        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        doThrow(new IllegalArgumentException("Filter error")).when(esDocumentMetadataFiltersProcessor)
            .process(any(ObjectNode.class), eq(context), anyMap());

        try (MockedStatic<SelectionManager> selectionManagerMockedStatic = mockStatic(SelectionManager.class);
             MockedStatic<ApiUtils> apiUtilsMockedStatic = mockStatic(ApiUtils.class)) {
            apiUtilsMockedStatic.when(() -> ApiUtils.getUserSession(httpSession)).thenReturn(userSession);
            selectionManagerMockedStatic.when(() -> SelectionManager.getManager(userSession)).thenReturn(selectionManager);
            when(selectionManager.getSelection(any())).thenReturn(new HashSet<>());

            // Should not throw exception, just log it
            esResponseProcessor.processResponse(context, httpSession, inputStream, outputStream,
                EsSearchEndpoints.SEARCH_ENDPOINT.toString(), "metadata", true, null);

            verify(esDocumentMetadataFiltersProcessor).process(any(ObjectNode.class), eq(context), anyMap());
            verify(esDocumentRemovePrivilegesProcessor).process(any(ObjectNode.class), eq(context), anyMap());
        }
    }
}
