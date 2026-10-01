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

package org.fao.geonet.repository;

import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.domain.ResourceUploadTaskStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for persistent resource upload tasks.
 */
public interface ResourceUploadTaskRepository
    extends GeonetRepository<ResourceUploadTask, String> {

    /**
     * Finds a task by its identifier and owning metadata record.
     *
     * @param id task identifier
     * @param metadataUuid metadata record UUID
     * @return the matching task, if one exists
     */
    Optional<ResourceUploadTask> findByIdAndMetadataUuid(
        String id,
        String metadataUuid
    );

    /**
     * Lists a user's tasks for one metadata record, newest first.
     *
     * @param metadataUuid metadata record UUID
     * @param ownerUserId submitting user's identifier
     * @return matching tasks ordered by submission time descending
     */
    List<ResourceUploadTask>
    findByMetadataUuidAndOwnerUserIdOrderBySubmittedDateTimeDesc(
        String metadataUuid,
        Integer ownerUserId
    );

    /**
     * Lists all tasks for one metadata record, newest first.
     *
     * @param metadataUuid metadata record UUID
     * @return matching tasks ordered by submission time descending
     */
    List<ResourceUploadTask>
    findByMetadataUuidOrderBySubmittedDateTimeDesc(
        String metadataUuid
    );

    /**
     * Checks whether a distributed filename claim is already present.
     *
     * @param claimKey deterministic filename claim key
     * @return {@code true} when the claim exists
     */
    boolean existsByClaimKey(String claimKey);

    /**
     * Finds task identifiers owned by a worker in the requested status.
     *
     * @param workerId application-process identifier
     * @param status task status to match
     * @return matching task identifiers
     */
    @Query(
        "SELECT t.id FROM ResourceUploadTask t " +
            "WHERE t.workerId = :workerId " +
            "AND t.status = :status"
    )
    List<String> findIdsByWorkerIdAndStatus(
        @Param("workerId") String workerId,
        @Param("status") ResourceUploadTaskStatus status
    );

    /**
     * Finds the current status of a task owned by a worker.
     *
     * <p>This is used after a conditional heartbeat or progress update affects
     * no rows. A task may have legitimately changed between active states, so
     * a zero update count alone is not sufficient reason to stop its local
     * execution.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @return the current status, or empty if the task is absent or owned by a
     * different worker
     */
    @Query(
        "SELECT t.status FROM ResourceUploadTask t " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId"
    )
    Optional<ResourceUploadTaskStatus> findStatusByIdAndWorkerId(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId
    );

    /**
     * Atomically starts a pending task owned by the worker.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param newStatus status assigned on success
     * @param now start and heartbeat time
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :newStatus, " +
            "t.startedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int startTask(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("newStatus") ResourceUploadTaskStatus newStatus,
        @Param("now") Date now
    );

    /**
     * Persists a resolved filename for an uploading task in a new transaction.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param filename resolved filename
     * @param now heartbeat time
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.filename = :filename, " +
            "t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int updateFilename(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("filename") String filename,
        @Param("now") Date now
    );

    /**
     * Atomically acquires a distributed filename claim in a new transaction.
     *
     * <p>The table's unique constraint rejects a claim already held by another
     * task.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param claimKey distributed filename claim
     * @param now heartbeat time
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.claimKey = :claimKey, " +
            "t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int acquireClaim(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("claimKey") String claimKey,
        @Param("now") Date now
    );

    /**
     * Persists the latest byte counts and heartbeat for an active execution.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatuses statuses that permit progress updates
     * @param bytesTransferred cumulative bytes transferred
     * @param totalBytes expected total bytes, or {@code -1} when unknown
     * @param now heartbeat time
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.bytesTransferred = :bytesTransferred, " +
            "t.totalBytes = :totalBytes, " +
            "t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status IN :expectedStatuses"
    )
    int updateProgress(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatuses")
        Collection<ResourceUploadTaskStatus> expectedStatuses,
        @Param("bytesTransferred") long bytesTransferred,
        @Param("totalBytes") long totalBytes,
        @Param("now") Date now
    );

    /**
     * Updates only the heartbeat of a task in the expected status.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param now heartbeat time
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int updateHeartbeat(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("now") Date now
    );

    /**
     * Enters the non-cancellable finalizing state in a new transaction.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param newStatus finalizing status
     * @param bytesTransferred cumulative bytes transferred
     * @param totalBytes expected total bytes, or {@code -1} when unknown
     * @param now transition and heartbeat time
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :newStatus, " +
            "t.bytesTransferred = :bytesTransferred, " +
            "t.totalBytes = :totalBytes, " +
            "t.lastHeartbeatDateTime = :now " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int startFinalizing(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("newStatus") ResourceUploadTaskStatus newStatus,
        @Param("bytesTransferred") long bytesTransferred,
        @Param("totalBytes") long totalBytes,
        @Param("now") Date now
    );

    /**
     * Completes a finalizing task and releases its distributed filename claim.
     *
     * <p>The default REQUIRED propagation deliberately joins the attachment
     * storage transaction so the attachment and completed task state commit or
     * roll back together.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatus required current status
     * @param newStatus completed status
     * @param filename stored resource filename
     * @param bytesTransferred cumulative bytes transferred
     * @param totalBytes expected total bytes
     * @param now completion and heartbeat time
     * @param releasedClaimKey unique non-active claim key
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :newStatus, " +
            "t.filename = :filename, " +
            "t.bytesTransferred = :bytesTransferred, " +
            "t.totalBytes = :totalBytes, " +
            "t.endedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now, " +
            "t.claimKey = :releasedClaimKey " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :expectedStatus"
    )
    int completeTask(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatus")
        ResourceUploadTaskStatus expectedStatus,
        @Param("newStatus") ResourceUploadTaskStatus newStatus,
        @Param("filename") String filename,
        @Param("bytesTransferred") long bytesTransferred,
        @Param("totalBytes") long totalBytes,
        @Param("now") Date now,
        @Param("releasedClaimKey") String releasedClaimKey
    );

    /**
     * Cancels a queued task before worker execution begins and releases its claim.
     *
     * @param taskId task identifier
     * @param pendingStatus required pending status
     * @param cancelledStatus terminal cancelled status
     * @param now cancellation and heartbeat time
     * @param releasedClaimKey unique non-active claim key
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :cancelledStatus, " +
            "t.endedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now, " +
            "t.claimKey = :releasedClaimKey " +
            "WHERE t.id = :taskId " +
            "AND t.status = :pendingStatus"
    )
    int cancelPendingTask(
        @Param("taskId") String taskId,
        @Param("pendingStatus")
        ResourceUploadTaskStatus pendingStatus,
        @Param("cancelledStatus")
        ResourceUploadTaskStatus cancelledStatus,
        @Param("now") Date now,
        @Param("releasedClaimKey") String releasedClaimKey
    );

    /**
     * Requests cooperative cancellation of an uploading task.
     *
     * @param taskId task identifier
     * @param uploadingStatus required uploading status
     * @param cancellingStatus cancellation-requested status
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :cancellingStatus " +
            "WHERE t.id = :taskId " +
            "AND t.status = :uploadingStatus"
    )
    int requestCancellation(
        @Param("taskId") String taskId,
        @Param("uploadingStatus")
        ResourceUploadTaskStatus uploadingStatus,
        @Param("cancellingStatus")
        ResourceUploadTaskStatus cancellingStatus
    );

    /**
     * Marks a cancelling task as cancelled and releases its claim.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param cancellingStatus required current status
     * @param cancelledStatus terminal cancelled status
     * @param bytesTransferred cumulative bytes transferred
     * @param totalBytes expected total bytes, or {@code -1} when unknown
     * @param now cancellation and heartbeat time
     * @param releasedClaimKey unique non-active claim key
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :cancelledStatus, " +
            "t.bytesTransferred = :bytesTransferred, " +
            "t.totalBytes = :totalBytes, " +
            "t.endedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now, " +
            "t.claimKey = :releasedClaimKey " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status = :cancellingStatus"
    )
    int markCancelled(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("cancellingStatus")
        ResourceUploadTaskStatus cancellingStatus,
        @Param("cancelledStatus")
        ResourceUploadTaskStatus cancelledStatus,
        @Param("bytesTransferred") long bytesTransferred,
        @Param("totalBytes") long totalBytes,
        @Param("now") Date now,
        @Param("releasedClaimKey") String releasedClaimKey
    );

    /**
     * Marks a worker-owned task as failed and releases its claim.
     *
     * @param taskId task identifier
     * @param workerId owning worker identifier
     * @param expectedStatuses statuses from which failure is allowed
     * @param failedStatus terminal failed status
     * @param error failure description
     * @param bytesTransferred cumulative bytes transferred
     * @param totalBytes expected total bytes, or {@code -1} when unknown
     * @param now failure and heartbeat time
     * @param releasedClaimKey unique non-active claim key
     * @return number of updated rows
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :failedStatus, " +
            "t.error = :error, " +
            "t.bytesTransferred = :bytesTransferred, " +
            "t.totalBytes = :totalBytes, " +
            "t.endedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now, " +
            "t.claimKey = :releasedClaimKey " +
            "WHERE t.id = :taskId " +
            "AND t.workerId = :workerId " +
            "AND t.status IN :expectedStatuses"
    )
    int failTask(
        @Param("taskId") String taskId,
        @Param("workerId") String workerId,
        @Param("expectedStatuses")
        Collection<ResourceUploadTaskStatus> expectedStatuses,
        @Param("failedStatus")
        ResourceUploadTaskStatus failedStatus,
        @Param("error") String error,
        @Param("bytesTransferred") long bytesTransferred,
        @Param("totalBytes") long totalBytes,
        @Param("now") Date now,
        @Param("releasedClaimKey") String releasedClaimKey
    );

    /**
     * Fails non-terminal tasks whose heartbeat is older than the supplied cutoff.
     *
     * @param nonTerminalStatuses statuses eligible for stale-task recovery
     * @param failedStatus terminal failed status
     * @param error failure description
     * @param now failure and heartbeat time
     * @param cutoff heartbeat cutoff
     * @param releasedClaimPrefix prefix used to create per-task released keys
     * @return number of updated rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE ResourceUploadTask t " +
            "SET t.status = :failedStatus, " +
            "t.error = :error, " +
            "t.endedDateTime = :now, " +
            "t.lastHeartbeatDateTime = :now, " +
            "t.claimKey = CONCAT(:releasedClaimPrefix, t.id) " +
            "WHERE t.status IN :nonTerminalStatuses " +
            "AND t.lastHeartbeatDateTime < :cutoff"
    )
    int failStaleTasks(
        @Param("nonTerminalStatuses")
        Collection<ResourceUploadTaskStatus> nonTerminalStatuses,
        @Param("failedStatus")
        ResourceUploadTaskStatus failedStatus,
        @Param("error") String error,
        @Param("now") Date now,
        @Param("cutoff") Date cutoff,
        @Param("releasedClaimPrefix") String releasedClaimPrefix
    );

    /**
     * Deletes terminal tasks older than the retention cutoff.
     *
     * @param terminalStatuses terminal statuses eligible for deletion
     * @param cutoff terminal-state retention cutoff
     * @return number of deleted rows
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
        "DELETE FROM ResourceUploadTask t " +
            "WHERE t.status IN :terminalStatuses " +
            "AND t.endedDateTime < :cutoff"
    )
    int deleteExpiredTasks(
        @Param("terminalStatuses")
        Collection<ResourceUploadTaskStatus> terminalStatuses,
        @Param("cutoff") Date cutoff
    );
}
