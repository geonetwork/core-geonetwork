//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.records.attachments;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.InputStreamLimitExceededException;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.domain.ResourceUploadTaskStatus;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for asynchronous resource-upload transaction boundaries.
 *
 * <p>The task repository and GeoNetwork transaction manager are real and use
 * the integration-test database. The remote resource store and executor are
 * controlled test collaborators so that worker execution occurs
 * deterministically on the test thread.
 *
 * <p>These tests specifically verify that the {@code FINALIZING} transition,
 * which uses a separate transaction, remains visible if the surrounding
 * attachment transaction fails and that the task can subsequently transition
 * from {@code FINALIZING} to {@code FAILED}.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AsyncResourceUploadServiceIntegrationTest
    extends AbstractServiceIntegrationTest {

    private static final String METADATA_UUID = "metadata-uuid";
    private static final String SOURCE_URL =
        "https://example.org/file.zip";
    private static final String FILENAME = "file.zip";
    private static final long FILE_SIZE = 100L;

    @Autowired
    private ResourceUploadTaskRepository repository;

    @Mock
    private ServiceManager serviceManager;

    @Mock
    private ThreadPoolTaskExecutor executor;

    @Mock
    private Store store;

    @Mock
    private ServiceContext requestContext;

    @Mock
    private ServiceContext workerContext;

    @Mock
    private UserSession userSession;

    @Mock
    private MetadataResource uploadedResource;

    private AsyncResourceUploadService service;

    private URL sourceUrl;

    /**
     * Creates a service using the real task repository and a synchronous test
     * executor.
     *
     * @throws Exception if the source URL or mocked worker cannot be prepared
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        repository.deleteAll();

        sourceUrl = new URL(SOURCE_URL);
        service = new AsyncResourceUploadService();

        ReflectionTestUtils.setField(
            service,
            "taskRepository",
            repository
        );
        ReflectionTestUtils.setField(
            service,
            "serviceManager",
            serviceManager
        );
        ReflectionTestUtils.setField(
            service,
            "executor",
            executor
        );

        when(requestContext.getUserSession())
            .thenReturn(userSession);
        when(requestContext.getLanguage())
            .thenReturn("eng");
        when(userSession.getUserIdAsInt())
            .thenReturn(42);

        when(serviceManager.createServiceContext(
            eq("attachmentAsyncUpload"),
            any()
        )).thenReturn(workerContext);

        /*
         * Run submitted work on the test thread. The service still uses the
         * real GeoNetwork transaction manager around the worker operation.
         */
        doAnswer(invocation -> {
            Runnable command = invocation.getArgument(0);
            command.run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        /*
         * Simulate the Store's normal listener interaction. Filename and
         * progress callbacks use the real repository methods and therefore
         * acquire the real distributed claim.
         */
        when(store.putResource(
            eq(workerContext),
            eq(METADATA_UUID),
            eq(sourceUrl),
            eq(MetadataResourceVisibility.PUBLIC),
            eq(false),
            any(ResourceUploadProgressListener.class)
        )).thenAnswer(invocation -> {
            ResourceUploadProgressListener listener =
                invocation.getArgument(5);

            listener.onFilenameResolved(FILENAME);
            listener.onProgress(FILE_SIZE, FILE_SIZE);

            return uploadedResource;
        });
    }

    /**
     * Stops the service-owned scheduler and removes persisted test tasks.
     */
    @After
    public void cleanUpUploadService() {
        if (service != null) {
            service.destroy();
        }

        repository.deleteAll();
    }

    /**
     * Verifies that a successful worker transaction progresses through all
     * worker states and releases its filename claim.
     *
     * @throws Exception if upload submission fails
     */
    @Test
    public void successfulUploadCommitsCompletedTask()
        throws Exception {

        when(uploadedResource.getFilename())
            .thenReturn(FILENAME);

        try (MockedStatic<ApiUtils> apiUtils =
                 mockStatic(ApiUtils.class)) {

            /*
             * Event publication is unrelated to the transaction behavior
             * under test and requires a complete metadata record.
             */
            apiUtils.when(() ->
                ApiUtils.getInternalId(METADATA_UUID, false)
            ).thenReturn(null);

            ResourceUploadTask submitted = service.submit(
                store,
                requestContext,
                METADATA_UUID,
                sourceUrl,
                MetadataResourceVisibility.PUBLIC,
                false
            );

            ResourceUploadTask persisted =
                repository.findById(submitted.getId())
                    .orElseThrow(() ->
                        new AssertionError(
                            "Completed upload task was not persisted."
                        )
                    );

            assertEquals(
                ResourceUploadTaskStatus.COMPLETED,
                persisted.getStatus()
            );
            assertEquals(FILENAME, persisted.getFilename());
            assertEquals(FILE_SIZE, persisted.getBytesTransferred());
            assertEquals(FILE_SIZE, persisted.getTotalBytes());
            assertNotNull(persisted.getStartedDateTime());
            assertNotNull(persisted.getEndedDateTime());
            assertNotNull(persisted.getLastHeartbeatDateTime());
            assertNull(persisted.getError());
            assertTrue(persisted.isTerminal());
            assertEquals(
                ResourceUploadTask.releasedClaimKey(
                    persisted.getId()
                ),
                persisted.getClaimKey()
            );

            verify(store).putResource(
                eq(workerContext),
                eq(METADATA_UUID),
                eq(sourceUrl),
                eq(MetadataResourceVisibility.PUBLIC),
                eq(false),
                any(ResourceUploadProgressListener.class)
            );
        }
    }

    /**
     * Verifies that a failure after the independently committed
     * {@code FINALIZING} transition changes the task to {@code FAILED} and
     * releases its filename claim.
     *
     * @throws Exception if upload submission fails
     */
    @Test
    public void failureAfterFinalizingCommitsFailedTask()
        throws Exception {

        /*
         * getFilename() is evaluated for completeTask() after the service has
         * already entered FINALIZING using REQUIRES_NEW.
         */
        when(uploadedResource.getFilename())
            .thenThrow(new IllegalStateException(
                "Failure after entering finalizing."
            ));

        try (MockedStatic<ApiUtils> apiUtils =
                 mockStatic(ApiUtils.class)) {

            apiUtils.when(() ->
                ApiUtils.getInternalId(METADATA_UUID, false)
            ).thenReturn(null);

            ResourceUploadTask submitted = service.submit(
                store,
                requestContext,
                METADATA_UUID,
                sourceUrl,
                MetadataResourceVisibility.PUBLIC,
                false
            );

            Optional<ResourceUploadTask> persistedResult =
                repository.findById(submitted.getId());

            assertTrue(persistedResult.isPresent());

            ResourceUploadTask persisted =
                persistedResult.get();

            assertEquals(
                ResourceUploadTaskStatus.FAILED,
                persisted.getStatus()
            );
            assertEquals(FILENAME, persisted.getFilename());
            assertEquals(FILE_SIZE, persisted.getBytesTransferred());
            assertEquals(FILE_SIZE, persisted.getTotalBytes());
            assertNotNull(persisted.getStartedDateTime());
            assertNotNull(persisted.getEndedDateTime());
            assertNotNull(persisted.getLastHeartbeatDateTime());
            assertEquals(
                "The upload failed. Please try again or contact an administrator.",
                persisted.getError()
            );
            assertTrue(persisted.isTerminal());
            assertEquals(
                ResourceUploadTask.releasedClaimKey(
                    persisted.getId()
                ),
                persisted.getClaimKey()
            );
        }
    }

    /**
     * Verifies that a size-limit failure gives the caller its specific reason.
     */
    @Test
    public void sizeLimitFailureReportsMaximumSize() throws Exception {
        InputStreamLimitExceededException sizeError =
            new InputStreamLimitExceededException(FILE_SIZE - 1);

        doThrow(sizeError).when(store).putResource(
            eq(workerContext),
            eq(METADATA_UUID),
            eq(sourceUrl),
            eq(MetadataResourceVisibility.PUBLIC),
            eq(false),
            any(ResourceUploadProgressListener.class)
        );

        ResourceUploadTask submitted = service.submit(
            store,
            requestContext,
            METADATA_UUID,
            sourceUrl,
            MetadataResourceVisibility.PUBLIC,
            false
        );

        ResourceUploadTask persisted = repository.findById(submitted.getId())
            .orElseThrow(() -> new AssertionError("Upload task was not persisted."));

        assertEquals(ResourceUploadTaskStatus.FAILED, persisted.getStatus());
        assertEquals(sizeError.getMessage(), persisted.getError());
    }
}
