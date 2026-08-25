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

import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.domain.ResourceUploadTaskStatus;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the node-local state maintained while an asynchronous resource upload
 * is being executed.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceUploadExecutionTest {

    private static final String TASK_ID = "task-id";
    private static final String METADATA_UUID = "metadata-uuid";
    private static final String WORKER_ID = "worker-id";

    @Mock
    private ResourceUploadTaskRepository repository;

    @Mock
    private Closeable stream;

    private ResourceUploadExecution execution;

    /**
     * Creates a fresh execution for each test.
     */
    @Before
    public void setUp() {
        execution = new ResourceUploadExecution(
            TASK_ID,
            METADATA_UUID,
            WORKER_ID,
            repository
        );
    }

    /**
     * Verifies the initial execution state.
     */
    @Test
    public void initializesExecutionState() {
        assertEquals(TASK_ID, execution.getTaskId());
        assertEquals(WORKER_ID, execution.getWorkerId());
        assertEquals(0, execution.getBytesTransferred());
        assertEquals(-1, execution.getTotalBytes());
        assertFalse(execution.hasStarted());
        assertFalse(execution.isCancelled());
        assertFalse(execution.isFailed());
    }

    /**
     * Verifies that live byte counters are updated by progress callbacks.
     */
    @Test
    public void recordsProgress() {
        execution.onProgress(50, 100);

        assertEquals(50, execution.getBytesTransferred());
        assertEquals(100, execution.getTotalBytes());
    }

    /**
     * Verifies that the worker can mark an execution as started.
     */
    @Test
    public void recordsWhenExecutionStarts() {
        execution.markStarted();

        assertTrue(execution.hasStarted());
    }

    /**
     * Verifies that the future associated with the execution is retained.
     */
    @Test
    public void storesFuture() {
        FutureTask<Void> future =
            new FutureTask<>(() -> null);

        execution.setFuture(future);

        assertSame(future, execution.getFuture());
    }

    /**
     * Verifies that resolving a filename persists it and acquires the
     * distributed filename claim.
     */
    @Test
    public void resolvedFilenameIsPersistedAndClaimed()
        throws Exception {

        when(repository.updateFilename(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq("file.zip"),
            any(Date.class)
        )).thenReturn(1);

        when(repository.acquireClaim(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq(ResourceUploadTask.activeClaimKey(
                METADATA_UUID,
                "file.zip"
            )),
            any(Date.class)
        )).thenReturn(1);

        execution.onFilenameResolved("file.zip");

        verify(repository).updateFilename(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq("file.zip"),
            any(Date.class)
        );

        verify(repository).acquireClaim(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq(ResourceUploadTask.activeClaimKey(
                METADATA_UUID,
                "file.zip"
            )),
            any(Date.class)
        );

        assertFalse(execution.isCancelled());
        assertFalse(execution.isFailed());
    }

    /**
     * Verifies that the execution is cancelled when its filename can no
     * longer be persisted because the database task is no longer uploading.
     */
    @Test
    public void filenameUpdateFailureCancelsExecution() {
        when(repository.updateFilename(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq("file.zip"),
            any(Date.class)
        )).thenReturn(0);

        assertThrows(
            CancellationException.class,
            () -> execution.onFilenameResolved("file.zip")
        );

        assertTrue(execution.isCancelled());

        verify(repository, never()).acquireClaim(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            any(String.class),
            any(Date.class)
        );
    }

    /**
     * Verifies that the execution is cancelled when the claim update no
     * longer matches an uploading task owned by the worker.
     */
    @Test
    public void claimUpdateFailureCancelsExecution() {
        when(repository.updateFilename(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq("file.zip"),
            any(Date.class)
        )).thenReturn(1);

        when(repository.acquireClaim(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            any(String.class),
            any(Date.class)
        )).thenReturn(0);

        assertThrows(
            CancellationException.class,
            () -> execution.onFilenameResolved("file.zip")
        );

        assertTrue(execution.isCancelled());
    }

    /**
     * Verifies that a database claim collision fails the upload, releases its
     * claim key, and reports a resource conflict to the caller.
     */
    @Test
    public void duplicateClaimFailsTask() {
        execution.onProgress(25, 100);

        when(repository.updateFilename(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            eq("file.zip"),
            any(Date.class)
        )).thenReturn(1);

        when(repository.acquireClaim(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.UPLOADING),
            any(String.class),
            any(Date.class)
        )).thenThrow(new DataIntegrityViolationException(
            "duplicate claim"
        ));

        ResourceAlreadyExistException exception = assertThrows(
            ResourceAlreadyExistException.class,
            () -> execution.onFilenameResolved("file.zip")
        );

        assertTrue(exception.getMessage().contains("file.zip"));
        assertTrue(exception.getMessage().contains(METADATA_UUID));
        assertTrue(execution.isFailed());
        assertFalse(execution.isCancelled());

        verify(repository).failTask(
            eq(TASK_ID),
            eq(WORKER_ID),
            eq(ResourceUploadTaskStatus.getFailableStatuses()),
            eq(ResourceUploadTaskStatus.FAILED),
            eq(exception.getMessage()),
            eq(25L),
            eq(100L),
            any(Date.class),
            eq(ResourceUploadTask.releasedClaimKey(TASK_ID))
        );
    }

    /**
     * Verifies that cancellation closes the active stream and cancels the
     * associated future.
     */
    @Test
    public void cancellationClosesStreamAndCancelsFuture()
        throws Exception {

        FutureTask<Void> future =
            new FutureTask<>(() -> null);

        execution.setFuture(future);
        execution.onStreamOpened(stream);

        execution.cancel();

        assertTrue(execution.isCancelled());
        assertTrue(future.isCancelled());
        verify(stream).close();
    }

    /**
     * Verifies that a stream opened after cancellation is closed immediately.
     */
    @Test
    public void streamOpenedAfterCancellationIsClosedImmediately()
        throws Exception {

        execution.cancel();
        execution.onStreamOpened(stream);

        verify(stream).close();
    }

    /**
     * Verifies that notifying the execution that a stream was closed removes
     * it from the active-stream slot.
     */
    @Test
    public void closedStreamIsNotClosedAgainDuringCancellation()
        throws Exception {

        execution.onStreamOpened(stream);
        execution.onStreamClosed(stream);

        execution.cancel();

        verify(stream, never()).close();
    }

    /**
     * Verifies that closing a different stream does not clear the active
     * stream.
     */
    @Test
    public void closingDifferentStreamDoesNotClearActiveStream()
        throws Exception {

        Closeable otherStream =
            org.mockito.Mockito.mock(Closeable.class);

        execution.onStreamOpened(stream);
        execution.onStreamClosed(otherStream);

        execution.cancel();

        verify(stream).close();
        verify(otherStream, never()).close();
    }

    /**
     * Verifies that repeated cancellation does not repeatedly close the same
     * stream.
     */
    @Test
    public void cancellationIsIdempotentForActiveStream()
        throws Exception {

        execution.onStreamOpened(stream);

        execution.cancel();
        execution.cancel();

        verify(stream, times(1)).close();
    }

    /**
     * Verifies that an I/O error while closing a cancelled stream does not
     * escape the cancellation path.
     */
    @Test
    public void closeFailureDoesNotEscapeCancellation()
        throws Exception {

        doThrow(new IOException("close failed"))
            .when(stream)
            .close();

        execution.onStreamOpened(stream);
        execution.cancel();

        assertTrue(execution.isCancelled());
        verify(stream).close();
    }
}
