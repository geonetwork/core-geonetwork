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

import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.fao.geonet.domain.ResourceUploadTaskStatus;

/**
 * Node-local execution resources and live counters for a resource upload.
 *
 * <p>The persistent task state is stored separately in the database.
 */
public class ResourceUploadExecution implements AsyncResourceUploadProgressListener {

    private final String taskId;
    private final String metadataUuid;
    private final String workerId;
    private final ResourceUploadTaskRepository repository;

    private final AtomicLong bytesTransferred = new AtomicLong();
    private final AtomicLong totalBytes = new AtomicLong(-1);
    private final AtomicReference<Closeable> activeStream =
        new AtomicReference<>();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean failed = new AtomicBoolean();

    private volatile FutureTask<Void> future;

    /**
     * Creates the node-local execution state for a persistent upload task.
     *
     * @param taskId persistent task identifier
     * @param metadataUuid UUID of the metadata record receiving the resource
     * @param workerId identifier of the application process executing the task
     * @param repository repository used for atomic task-state transitions
     */
    public ResourceUploadExecution(
        String taskId,
        String metadataUuid,
        String workerId,
        ResourceUploadTaskRepository repository
    ) {
        this.taskId = taskId;
        this.metadataUuid = metadataUuid;
        this.workerId = workerId;
        this.repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    public String getTaskId() {
        return taskId;
    }

    /** {@inheritDoc} */
    @Override
    public String getWorkerId() {
        return workerId;
    }

    /**
     * Returns the latest in-memory transferred-byte count.
     *
     * @return cumulative bytes transferred
     */
    public long getBytesTransferred() {
        return bytesTransferred.get();
    }

    /**
     * Returns the latest expected source size.
     *
     * @return expected bytes, or {@code -1} when unknown
     */
    public long getTotalBytes() {
        return totalBytes.get();
    }

    /**
     * Returns whether the executor has begun running this upload.
     *
     * @return {@code true} once worker execution has started
     */
    public boolean hasStarted() {
        return started.get();
    }

    /**
     * Marks this execution as started by its worker.
     */
    public void markStarted() {
        started.set(true);
    }

    /**
     * Returns whether filename-claim acquisition already failed the task.
     *
     * @return {@code true} when this execution has recorded a claim failure
     */
    public boolean isFailed() {
        return failed.get();
    }

    /**
     * Returns the executor task associated with this upload.
     *
     * @return executor task, or {@code null} before submission
     */
    public FutureTask<Void> getFuture() {
        return future;
    }

    /**
     * Associates the executor task used to run this upload.
     *
     * @param future executor task
     */
    public void setFuture(FutureTask<Void> future) {
        this.future = future;
    }

    /** {@inheritDoc} */
    @Override
    public void onProgress(long bytesTransferred, long totalBytes) {
        this.bytesTransferred.set(bytesTransferred);
        this.totalBytes.set(totalBytes);
    }

    /**
     * Persists the resolved filename and atomically acquires its distributed
     * metadata-and-filename claim.
     *
     * @param filename resolved resource filename
     * @throws ResourceAlreadyExistException if another task already owns the
     *         distributed filename claim
     * @throws CancellationException if the persistent task is no longer uploading
     */
    @Override
    public void onFilenameResolved(String filename) throws ResourceAlreadyExistException {

        Date now = new Date();

        int filenameUpdated = repository.updateFilename(
            taskId,
            workerId,
            ResourceUploadTaskStatus.UPLOADING,
            filename,
            now
        );

        if (filenameUpdated == 0) {
            cancelled.set(true);
            throw new CancellationException(
                "Resource upload task " + taskId +
                    " is no longer uploading."
            );
        }

        String claimKey = ResourceUploadTask.activeClaimKey(
            metadataUuid,
            filename
        );

        try {
            int claimUpdated = repository.acquireClaim(
                taskId,
                workerId,
                ResourceUploadTaskStatus.UPLOADING,
                claimKey,
                now
            );

            if (claimUpdated == 0) {
                cancelled.set(true);
                throw new CancellationException(
                    "Resource upload task " + taskId +
                        " could not acquire its filename claim."
                );
            }
        } catch (DataIntegrityViolationException e) {
            String message = String.format(
                "An upload for filename '%s' is already in progress " +
                    "for record '%s'. Wait for completion before retrying.",
                filename,
                metadataUuid
            );

            failed.set(true);

            repository.failTask(
                taskId,
                workerId,
                ResourceUploadTaskStatus.getFailableStatuses(),
                ResourceUploadTaskStatus.FAILED,
                message,
                bytesTransferred.get(),
                totalBytes.get(),
                new Date(),
                ResourceUploadTask.releasedClaimKey(taskId)
            );

            throw new ResourceAlreadyExistException(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    /** {@inheritDoc} */
    @Override
    public void onStreamOpened(Closeable stream) {
        activeStream.set(stream);

        if (cancelled.get()) {
            closeStream(stream);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onStreamClosed(Closeable stream) {
        activeStream.compareAndSet(stream, null);
    }

    /**
     * Requests node-local cancellation, closes the active network stream, and
     * cancels queued executor work without interrupting a running worker thread.
     */
    public void cancel() {
        cancelled.set(true);

        Closeable stream = activeStream.getAndSet(null);
        if (stream != null) {
            closeStream(stream);
        }

        FutureTask<Void> uploadFuture = future;
        if (uploadFuture != null) {
            uploadFuture.cancel(false);
        }
    }

    private void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // Cancellation has already been requested.
        }
    }
}
