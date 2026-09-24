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

package org.fao.geonet.api.records;

import jeeves.server.UserSession;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Batch tag endpoints must apply the changes to the approved record and to its working copy.
 */
public class MetadataTagApiBatchTest {

    private static final String UUID = "batch-tag-uuid";

    private final MetadataTagApi api = new MetadataTagApi();
    private final MetadataCategory cat1 = category(1);
    private final MetadataCategory cat2 = category(2);
    private final MetadataCategory cat3 = category(3);
    private Metadata approved;
    private MetadataDraft draft;
    private MockedStatic<ApiUtils> apiUtils;
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Before
    public void setUp() {
        approved = new Metadata();
        approved.setId(100);
        approved.setUuid(UUID);
        approved.getCategories().add(cat1);
        approved.getCategories().add(cat2);

        draft = new MetadataDraft();
        draft.setId(101);
        draft.setUuid(UUID);
        draft.getCategories().add(cat1);
        draft.getCategories().add(cat2);

        api.metadataRepository = mock(MetadataRepository.class);
        api.metadataDraftRepository = mock(MetadataDraftRepository.class);
        api.categoryRepository = mock(MetadataCategoryRepository.class);
        api.metadataManager = mock(IMetadataManager.class);
        api.accessMan = mock(AccessManager.class);
        api.searchManager = mock(EsSearchManager.class);
        api.dataManager = mock(DataManager.class);

        when(api.metadataRepository.findOneByUuid(UUID)).thenReturn(approved);
        when(api.metadataDraftRepository.findOneByUuid(UUID)).thenReturn(draft);
        for (MetadataCategory c : new MetadataCategory[]{cat1, cat2, cat3}) {
            when(api.categoryRepository.findById(c.getId())).thenReturn(Optional.of(c));
        }
        try {
            when(api.accessMan.canEdit(any(), anyString())).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ApplicationContextHolder.set(mock(ConfigurableApplicationContext.class));

        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        UserSession userSession = mock(UserSession.class);
        when(userSession.getUserIdAsInt()).thenReturn(1);

        apiUtils = mockStatic(ApiUtils.class);
        apiUtils.when(() -> ApiUtils.getUserSession(any())).thenReturn(userSession);
        apiUtils.when(() -> ApiUtils.createServiceContext(any(HttpServletRequest.class))).thenReturn(null);
        apiUtils.when(() -> ApiUtils.getUuidsParameterOrSelection(any(), any(), any()))
            .thenAnswer(i -> i.getArgument(0) == null ? Collections.emptySet() : Set.of((String[]) i.getArgument(0)));
    }

    @After
    public void tearDown() {
        apiUtils.close();
        ApplicationContextHolder.clear();
    }

    @Test
    public void tagRecordsAddsToApprovedAndWorkingCopy() throws Exception {
        MetadataProcessingReport report = api.tagRecords(new String[]{UUID}, null,
            new Integer[]{3}, new Integer[]{1}, false, request, null);

        assertEquals(Set.of(2, 3), ids(approved.getCategories()));
        assertEquals(Set.of(2, 3), ids(draft.getCategories()));
        verify(api.metadataManager).save(approved);
        verify(api.metadataDraftRepository).save(draft);
        assertEquals(1, report.getNumberOfRecordsProcessed());
    }

    @Test
    public void tagRecordsOnlyAddIsSavedAndCounted() throws Exception {
        MetadataProcessingReport report = api.tagRecords(new String[]{UUID}, null,
            new Integer[]{3}, null, false, request, null);

        assertEquals(Set.of(1, 2, 3), ids(approved.getCategories()));
        verify(api.metadataManager).save(approved);
        assertEquals(1, report.getNumberOfRecordsProcessed());
    }

    @Test
    public void deleteTagForRecordsRemovesOnlyGivenIds() throws Exception {
        api.deleteTagForRecords(new String[]{UUID}, null, new Integer[]{1}, request, null);

        assertEquals(Set.of(2), ids(approved.getCategories()));
        assertEquals(Set.of(2), ids(draft.getCategories()));
    }

    @Test
    public void deleteTagForRecordsWithoutIdsRemovesAll() throws Exception {
        api.deleteTagForRecords(new String[]{UUID}, null, null, request, null);

        assertEquals(Set.of(), ids(approved.getCategories()));
        assertEquals(Set.of(), ids(draft.getCategories()));
    }

    @Test
    public void unknownRecordIsReportedNotFailed() throws Exception {
        MetadataProcessingReport report = api.tagRecords(new String[]{"unknown"}, null,
            new Integer[]{3}, null, false, request, null);

        assertEquals(1, report.getNumberOfNullRecords());
        assertEquals(0, report.getErrors().size());
    }

    private static MetadataCategory category(int id) {
        MetadataCategory c = new MetadataCategory();
        c.setId(id);
        c.setName("cat" + id);
        return c;
    }

    private static Set<Integer> ids(Set<MetadataCategory> categories) {
        return categories.stream().map(MetadataCategory::getId).collect(Collectors.toSet());
    }
}
