/*
 * =============================================================================
 * === Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * === United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * === and United Nations Environment Programme (UNEP)
 * ===
 * === This program is free software; you can redistribute it and/or modify
 * === it under the terms of the GNU General Public License as published by
 * === the Free Software Foundation; either version 2 of the License, or (at
 * === your option) any later version.
 * ===
 * === This program is distributed in the hope that it will be useful, but
 * === WITHOUT ANY WARRANTY; without even the implied warranty of
 * === MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * === General Public License for more details.
 * ===
 * === You should have received a copy of the GNU General Public License
 * === along with this program; if not, write to the Free Software
 * === Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * === Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * === Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.api.records.attachments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.FileMimetypeChecker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the asynchronous resource-upload API contract and delegation to the
 * upload service.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachmentsApiTest {

    private static final String METADATA_UUID = "metadata-uuid";
    private static final String TASK_ID = "task-id";

    @Mock
    private FileMimetypeChecker fileMimetypeChecker;

    @Mock
    private SettingManager settingManager;

    @Mock
    private IMetadataManager metadataManager;

    @Mock
    private IMetadataIndexer metadataIndexer;

    @Mock
    private AsyncResourceUploadService uploadService;

    @Mock
    private Store store;

    @Mock
    private ServiceContext serviceContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession httpSession;

    @Mock
    private UserSession userSession;

    private AttachmentsApi api;

    /**
     * Creates the API with mocked collaborators.
     */
    @Before
    public void setUp() {
        api = new AttachmentsApi(
            fileMimetypeChecker,
            settingManager,
            metadataManager,
            metadataIndexer,
            uploadService
        );
        api.setStore(store);
    }

    /**
     * Verifies that creating an asynchronous task returns {@code 202} and the
     * task returned by the upload service.
     */
    @Test
    public void createUploadTaskReturnsAccepted() throws Exception {
        URL url = new URL("https://example.org/file.zip");
        ResourceUploadTask task = task();

        when(uploadService.submit(
            store,
            serviceContext,
            METADATA_UUID,
            url,
            MetadataResourceVisibility.PUBLIC,
            false
        )).thenReturn(task);

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.createServiceContext(request))
                .thenReturn(serviceContext);

            ResponseEntity<ResourceUploadTask> response =
                api.createUploadTask(
                    METADATA_UUID,
                    MetadataResourceVisibility.PUBLIC,
                    url,
                    false,
                    request
                );

            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertSame(task, response.getBody());

            apiUtils.verify(() ->
                ApiUtils.canEditRecord(
                    METADATA_UUID,
                    false,
                    request
                )
            );
        }
    }

    /**
     * Verifies that task lookup delegates ownership enforcement to the upload
     * service and returns its result.
     */
    @Test
    public void getUploadTaskReturnsAuthorizedTask() throws Exception {
        ResourceUploadTask task = task();

        when(uploadService.getOwnedTaskOrThrow(
            METADATA_UUID,
            TASK_ID,
            request
        )).thenReturn(task);

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            assertSame(
                task,
                api.getUploadTask(
                    METADATA_UUID,
                    TASK_ID,
                    request
                )
            );

            apiUtils.verify(() ->
                ApiUtils.canEditRecord(METADATA_UUID, request)
            );
        }
    }

    /**
     * Verifies that task lookup preserves not-found and ownership failures
     * raised by the upload service.
     */
    @Test
    public void getUploadTaskPropagatesLookupAndOwnershipFailures()
        throws Exception {

        when(uploadService.getOwnedTaskOrThrow(
            METADATA_UUID,
            "missing",
            request
        )).thenThrow(new ResourceNotFoundException("missing"));

        when(uploadService.getOwnedTaskOrThrow(
            METADATA_UUID,
            "forbidden",
            request
        )).thenThrow(new SecurityException("forbidden"));

        try (MockedStatic<ApiUtils> ignored = mockStatic(ApiUtils.class)) {
            assertThrows(
                ResourceNotFoundException.class,
                () -> api.getUploadTask(
                    METADATA_UUID,
                    "missing",
                    request
                )
            );

            assertThrows(
                SecurityException.class,
                () -> api.getUploadTask(
                    METADATA_UUID,
                    "forbidden",
                    request
                )
            );
        }
    }

    /**
     * Verifies that task listing uses the current session and returns the
     * service's visibility-filtered result.
     */
    @Test
    public void getUploadTasksReturnsVisibleTasks() throws Exception {
        List<ResourceUploadTask> tasks =
            Collections.singletonList(task());

        when(request.getSession()).thenReturn(httpSession);
        when(uploadService.listUploadsForUser(
            METADATA_UUID,
            userSession
        )).thenReturn(tasks);

        try (MockedStatic<ApiUtils> apiUtils = mockStatic(ApiUtils.class)) {
            apiUtils.when(() -> ApiUtils.getUserSession(httpSession))
                .thenReturn(userSession);

            assertSame(
                tasks,
                api.getUploadTasks(METADATA_UUID, request)
            );
        }
    }

    /**
     * Verifies that accepted cancellation returns {@code 200} and rejected
     * cancellation returns {@code 409}.
     */
    @Test
    public void cancelUploadTaskReturnsAcceptedOrConflict()
        throws Exception {

        when(uploadService.cancel(
            METADATA_UUID,
            TASK_ID,
            request
        )).thenReturn(true, false);

        try (MockedStatic<ApiUtils> ignored = mockStatic(ApiUtils.class)) {
            ResponseEntity<Void> accepted = api.cancelUploadTask(
                METADATA_UUID,
                TASK_ID,
                request
            );
            ResponseEntity<Void> conflict = api.cancelUploadTask(
                METADATA_UUID,
                TASK_ID,
                request
            );

            assertEquals(HttpStatus.OK, accepted.getStatusCode());
            assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        }

        verify(uploadService, times(2)).cancel(
            METADATA_UUID,
            TASK_ID,
            request
        );
    }

    /**
     * Verifies that worker coordination and ownership fields are not exposed
     * in task API responses.
     */
    @Test
    public void taskSerializationHidesInternalFields() throws Exception {
        ResourceUploadTask task = task()
            .setLastHeartbeatDateTime(new Date());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(
            objectMapper.writeValueAsString(task)
        );

        assertTrue(json.has("id"));
        assertTrue(json.has("url"));
        assertFalse(json.has("ownerUserId"));
        assertFalse(json.has("workerId"));
        assertFalse(json.has("claimKey"));
        assertFalse(json.has("lastHeartbeatDateTime"));
    }

    /**
     * Creates an upload task suitable for API tests.
     */
    private ResourceUploadTask task() {
        return ResourceUploadTask.create(
            METADATA_UUID,
            42,
            "https://example.org/file.zip",
            MetadataResourceVisibility.PUBLIC,
            false,
            "worker-id"
        ).setId(TASK_ID);
    }
}
