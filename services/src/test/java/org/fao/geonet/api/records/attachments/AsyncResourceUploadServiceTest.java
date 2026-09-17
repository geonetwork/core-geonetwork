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

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.domain.ResourceUploadTaskStatus;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests asynchronous upload submission, authorization, cancellation, executor
 * handling, and transaction-aware scheduling.
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncResourceUploadServiceTest {

    private static final String METADATA_UUID = "metadata-uuid";
    private static final String SOURCE_URL =
        "https://example.org/file.zip";

    @Mock
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
    private UserSession userSession;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession httpSession;

    private AsyncResourceUploadService service;

    private final AtomicReference<ResourceUploadTask> savedTask =
        new AtomicReference<>();

    /**
     * Creates the service and injects its collaborators.
     */
    @Before
    public void setUp() {
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

        lenient().when(requestContext.getUserSession())
            .thenReturn(userSession);
        lenient().when(requestContext.getLanguage())
            .thenReturn("eng");
        lenient().when(userSession.getUserIdAsInt())
            .thenReturn(42);

        lenient().when(repository.save(any(ResourceUploadTask.class)))
            .thenAnswer(invocation -> {
                ResourceUploadTask task = invocation.getArgument(0);
                savedTask.set(task);
                return task;
            });

        lenient().when(repository.findById(anyString()))
            .thenAnswer(invocation ->
                Optional.ofNullable(savedTask.get())
            );
    }

    /**
     * Clears thread-bound transaction state after each test.
     */
    @After
    public void tearDown() {
        if (TransactionSynchronizationManager
            .isSynchronizationActive()) {

            TransactionSynchronizationManager
                .clearSynchronization();
        }

        TransactionSynchronizationManager
            .setActualTransactionActive(false);
    }

    /**
     * Verifies that submission creates a pending persistent task and submits
     * its execution immediately when no outer transaction exists.
     */
    @Test
    public void submissionPersistsPendingTaskAndExecutesImmediately()
        throws Exception {

        ResourceUploadTask result = service.submit(
            store,
            requestContext,
            METADATA_UUID,
            new URL(SOURCE_URL),
            MetadataResourceVisibility.PUBLIC,
            false
        );

        assertNotNull(result);
        assertEquals(ResourceUploadTaskStatus.PENDING, result.getStatus());
        assertEquals(METADATA_UUID, result.getMetadataUuid());
        assertEquals(Integer.valueOf(42), result.getOwnerUserId());
        assertEquals(SOURCE_URL, result.getSourceUrl());
        assertEquals(
            MetadataResourceVisibility.PUBLIC,
            result.getVisibility()
        );
        assertEquals(Boolean.FALSE, result.getApproved());

        verify(repository).save(result);
        verify(executor).execute(any(FutureTask.class));
    }

    /**
     * Verifies that an authenticated session is required for submission.
     */
    @Test
    public void submissionRequiresAuthenticatedUser() throws Exception {
        when(requestContext.getUserSession()).thenReturn(null);

        assertThrows(
            IllegalStateException.class,
            () -> service.submit(
                store,
                requestContext,
                METADATA_UUID,
                new URL(SOURCE_URL),
                MetadataResourceVisibility.PUBLIC,
                false
            )
        );

        verify(repository, never())
            .save(any(ResourceUploadTask.class));
        verify(executor, never()).execute(any(Runnable.class));
    }

    /**
     * Verifies that worker execution is deferred until the transaction that
     * created the task has committed.
     */
    @Test
    public void submissionIsDeferredUntilTransactionCommits()
        throws Exception {

        beginTransactionSynchronization();

        service.submit(
            store,
            requestContext,
            METADATA_UUID,
            new URL(SOURCE_URL),
            MetadataResourceVisibility.PUBLIC,
            false
        );

        verify(executor, never()).execute(any(Runnable.class));

        List<TransactionSynchronization> synchronizations =
            TransactionSynchronizationManager.getSynchronizations();

        assertEquals(1, synchronizations.size());

        synchronizations.get(0).afterCommit();

        verify(executor).execute(any(FutureTask.class));
    }

    /**
     * Verifies that a rolled-back task-creation transaction does not submit
     * its worker execution.
     */
    @Test
    public void rolledBackSubmissionIsNotExecuted() throws Exception {
        beginTransactionSynchronization();

        ResourceUploadTask task = service.submit(
            store,
            requestContext,
            METADATA_UUID,
            new URL(SOURCE_URL),
            MetadataResourceVisibility.PUBLIC,
            false
        );

        List<TransactionSynchronization> synchronizations =
            TransactionSynchronizationManager.getSynchronizations();

        synchronizations.get(0).afterCompletion(
            TransactionSynchronization.STATUS_ROLLED_BACK
        );

        verify(executor, never()).execute(any(Runnable.class));
        assertFalse(localExecutions().containsKey(task.getId()));
    }

    /**
     * Verifies that executor saturation fails the task rather than leaving it
     * permanently pending.
     */
    @Test
    public void executorRejectionFailsTask() throws Exception {
        doThrow(new RejectedExecutionException("queue full"))
            .when(executor)
            .execute(any(Runnable.class));

        ResourceUploadTask task = service.submit(
            store,
            requestContext,
            METADATA_UUID,
            new URL(SOURCE_URL),
            MetadataResourceVisibility.PUBLIC,
            false
        );

        verify(repository).failTask(
            eq(task.getId()),
            anyString(),
            eq(ResourceUploadTaskStatus.getFailableStatuses()),
            eq(ResourceUploadTaskStatus.FAILED),
            eq(
                "The server is too busy to process this upload right now. " +
                    "Please retry later."
            ),
            eq(0L),
            eq(-1L),
            any(),
            eq(ResourceUploadTask.releasedClaimKey(task.getId()))
        );

        assertFalse(localExecutions().containsKey(task.getId()));
    }

    /**
     * Verifies immediate cancellation of a task that has not begun running.
     */
    @Test
    public void pendingTaskIsCancelledImmediately() {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.PENDING,
            42
        );

        when(repository.cancelPendingTask(
            eq(task.getId()),
            eq(ResourceUploadTaskStatus.PENDING),
            eq(ResourceUploadTaskStatus.CANCELLED),
            any(),
            eq(ResourceUploadTask.releasedClaimKey(task.getId()))
        )).thenReturn(1);

        assertTrue(service.cancel(task));

        verify(repository).cancelPendingTask(
            eq(task.getId()),
            eq(ResourceUploadTaskStatus.PENDING),
            eq(ResourceUploadTaskStatus.CANCELLED),
            any(),
            eq(ResourceUploadTask.releasedClaimKey(task.getId()))
        );

        verify(repository, never()).requestCancellation(
            anyString(),
            any(ResourceUploadTaskStatus.class),
            any(ResourceUploadTaskStatus.class)
        );
    }

    /**
     * Verifies that a running upload transitions to cancelling.
     */
    @Test
    public void uploadingTaskRequestsCancellation() {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.UPLOADING,
            42
        );

        when(repository.requestCancellation(
            task.getId(),
            ResourceUploadTaskStatus.UPLOADING,
            ResourceUploadTaskStatus.CANCELLING
        )).thenReturn(1);

        assertTrue(service.cancel(task));

        verify(repository).requestCancellation(
            task.getId(),
            ResourceUploadTaskStatus.UPLOADING,
            ResourceUploadTaskStatus.CANCELLING
        );
    }

    /**
     * Verifies that an already cancelling task treats repeated cancellation
     * as accepted.
     */
    @Test
    public void repeatedCancellationIsAccepted() {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.CANCELLING,
            42
        );

        assertTrue(service.cancel(task));

        verify(repository, never()).cancelPendingTask(
            anyString(),
            any(ResourceUploadTaskStatus.class),
            any(ResourceUploadTaskStatus.class),
            any(),
            anyString()
        );

        verify(repository, never()).requestCancellation(
            anyString(),
            any(ResourceUploadTaskStatus.class),
            any(ResourceUploadTaskStatus.class)
        );
    }

    /**
     * Verifies that finalizing and terminal tasks cannot be cancelled.
     */
    @Test
    public void finalizingAndTerminalTasksRejectCancellation() {
        assertFalse(service.cancel(task(
            ResourceUploadTaskStatus.FINALIZING,
            42
        )));

        assertFalse(service.cancel(task(
            ResourceUploadTaskStatus.COMPLETED,
            42
        )));

        assertFalse(service.cancel(task(
            ResourceUploadTaskStatus.FAILED,
            42
        )));

        assertFalse(service.cancel(task(
            ResourceUploadTaskStatus.CANCELLED,
            42
        )));
    }

    /**
     * Verifies that a concurrent status change causes cancellation to report
     * a conflict.
     */
    @Test
    public void cancellationReturnsFalseWhenDatabaseUpdateLosesRace() {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.UPLOADING,
            42
        );

        when(repository.requestCancellation(
            task.getId(),
            ResourceUploadTaskStatus.UPLOADING,
            ResourceUploadTaskStatus.CANCELLING
        )).thenReturn(0);

        assertFalse(service.cancel(task));
    }

    /**
     * Verifies that an administrator sees every task for the record.
     */
    @Test
    public void administratorListsAllRecordTasks() {
        ResourceUploadTask first = task(
            ResourceUploadTaskStatus.PENDING,
            42
        );
        ResourceUploadTask second = task(
            ResourceUploadTaskStatus.COMPLETED,
            99
        );

        when(userSession.getProfile())
            .thenReturn(Profile.Administrator);

        when(repository
            .findByMetadataUuidOrderBySubmittedDateTimeDesc(
                METADATA_UUID
            ))
            .thenReturn(Arrays.asList(first, second));

        List<ResourceUploadTask> result =
            service.listUploadsForUser(
                METADATA_UUID,
                userSession
            );

        assertEquals(Arrays.asList(first, second), result);

        verify(repository)
            .findByMetadataUuidOrderBySubmittedDateTimeDesc(
                METADATA_UUID
            );
    }

    /**
     * Verifies that non-administrator users see only their own tasks.
     */
    @Test
    public void regularUserListsOnlyOwnedTasks() {
        ResourceUploadTask owned = task(
            ResourceUploadTaskStatus.PENDING,
            42
        );

        when(userSession.getProfile())
            .thenReturn(Profile.Editor);

        when(repository
            .findByMetadataUuidAndOwnerUserIdOrderBySubmittedDateTimeDesc(
                METADATA_UUID,
                42
            ))
            .thenReturn(Collections.singletonList(owned));

        List<ResourceUploadTask> result =
            service.listUploadsForUser(
                METADATA_UUID,
                userSession
            );

        assertEquals(Collections.singletonList(owned), result);

        verify(repository)
            .findByMetadataUuidAndOwnerUserIdOrderBySubmittedDateTimeDesc(
                METADATA_UUID,
                42
            );
    }

    /**
     * Verifies that an unauthenticated request receives no task listing.
     */
    @Test
    public void unauthenticatedUserReceivesEmptyTaskList() {
        assertTrue(
            service.listUploadsForUser(
                METADATA_UUID,
                null
            ).isEmpty()
        );

        verify(repository, never())
            .findByMetadataUuidOrderBySubmittedDateTimeDesc(
                anyString()
            );

        verify(repository, never())
            .findByMetadataUuidAndOwnerUserIdOrderBySubmittedDateTimeDesc(
                anyString(),
                any()
            );
    }

    /**
     * Verifies that the task owner can retrieve a task.
     */
    @Test
    public void ownerCanRetrieveTask() throws Exception {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.UPLOADING,
            42
        );

        when(repository.findByIdAndMetadataUuid(
            task.getId(),
            METADATA_UUID
        )).thenReturn(Optional.of(task));

        when(request.getSession()).thenReturn(httpSession);

        try (MockedStatic<ApiUtils> apiUtils =
                 org.mockito.Mockito.mockStatic(ApiUtils.class)) {

            apiUtils.when(() ->
                ApiUtils.getUserSession(httpSession)
            ).thenReturn(userSession);

            when(userSession.getProfile())
                .thenReturn(Profile.Editor);
            when(userSession.getUserIdAsInt())
                .thenReturn(42);

            assertEquals(
                task,
                service.getOwnedTaskOrThrow(
                    METADATA_UUID,
                    task.getId(),
                    request
                )
            );
        }
    }

    /**
     * Verifies that administrators can retrieve tasks owned by other users.
     */
    @Test
    public void administratorCanRetrieveAnotherUsersTask()
        throws Exception {

        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.UPLOADING,
            99
        );

        when(repository.findByIdAndMetadataUuid(
            task.getId(),
            METADATA_UUID
        )).thenReturn(Optional.of(task));

        when(request.getSession()).thenReturn(httpSession);

        try (MockedStatic<ApiUtils> apiUtils =
                 org.mockito.Mockito.mockStatic(ApiUtils.class)) {

            apiUtils.when(() ->
                ApiUtils.getUserSession(httpSession)
            ).thenReturn(userSession);

            when(userSession.getProfile())
                .thenReturn(Profile.Administrator);

            assertEquals(
                task,
                service.getOwnedTaskOrThrow(
                    METADATA_UUID,
                    task.getId(),
                    request
                )
            );
        }
    }

    /**
     * Verifies that a user cannot retrieve another user's task.
     */
    @Test
    public void nonOwnerCannotRetrieveTask() {
        ResourceUploadTask task = task(
            ResourceUploadTaskStatus.UPLOADING,
            99
        );

        when(repository.findByIdAndMetadataUuid(
            task.getId(),
            METADATA_UUID
        )).thenReturn(Optional.of(task));

        when(request.getSession()).thenReturn(httpSession);

        try (MockedStatic<ApiUtils> apiUtils =
                 org.mockito.Mockito.mockStatic(ApiUtils.class)) {

            apiUtils.when(() ->
                ApiUtils.getUserSession(httpSession)
            ).thenReturn(userSession);

            when(userSession.getProfile())
                .thenReturn(Profile.Editor);
            when(userSession.getUserIdAsInt())
                .thenReturn(42);
            when(userSession.getUsername())
                .thenReturn("editor");

            assertThrows(
                SecurityException.class,
                () -> service.getOwnedTaskOrThrow(
                    METADATA_UUID,
                    task.getId(),
                    request
                )
            );
        }
    }

    /**
     * Verifies that an unknown task returns a resource-not-found error.
     */
    @Test
    public void unknownTaskThrowsResourceNotFoundException() {
        when(repository.findByIdAndMetadataUuid(
            "missing",
            METADATA_UUID
        )).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.getOwnedTaskOrThrow(
                METADATA_UUID,
                "missing",
                request
            )
        );
    }

    /**
     * Marks transaction synchronization as active for after-commit tests.
     */
    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager
            .setActualTransactionActive(true);
        TransactionSynchronizationManager
            .initSynchronization();
    }

    /**
     * Returns the service's node-local execution registry.
     */
    @SuppressWarnings("unchecked")
    private ConcurrentMap<String, ResourceUploadExecution>
    localExecutions() {

        return (ConcurrentMap<String, ResourceUploadExecution>)
            ReflectionTestUtils.getField(
                service,
                "executions"
            );
    }

    /**
     * Creates a task in the requested state.
     */
    private ResourceUploadTask task(
        ResourceUploadTaskStatus status,
        int ownerUserId
    ) {
        return ResourceUploadTask.create(
            METADATA_UUID,
            ownerUserId,
            SOURCE_URL,
            MetadataResourceVisibility.PUBLIC,
            false,
            "worker"
        ).setStatus(status);
    }
}
