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

import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.ResourceUploadTask;
import org.fao.geonet.domain.ResourceUploadTaskStatus;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/**
 * Tests atomic state transitions, distributed claims, heartbeat recovery, and
 * retention queries for persistent resource upload tasks.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ResourceUploadTaskRepositoryTest
    extends AbstractSpringDataTest {

    @Autowired
    private ResourceUploadTaskRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void startsOnlyPendingTaskOwnedByWorker() {
        ResourceUploadTask task = saveTask("worker");

        assertEquals(
            1,
            repository.startTask(
                task.getId(),
                "worker",
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.UPLOADING,
                new Date()
            )
        );

        assertEquals(
            ResourceUploadTaskStatus.UPLOADING,
            reload(task).getStatus()
        );

        assertEquals(
            0,
            repository.startTask(
                task.getId(),
                "worker",
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.UPLOADING,
                new Date()
            )
        );
    }

    @Test
    public void progressUpdateRequiresWorkerAndAllowedStatus() {
        ResourceUploadTask task = saveTask("worker");

        repository.startTask(
            task.getId(),
            "worker",
            ResourceUploadTaskStatus.PENDING,
            ResourceUploadTaskStatus.UPLOADING,
            new Date()
        );

        assertEquals(
            1,
            repository.updateProgress(
                task.getId(),
                "worker",
                Arrays.asList(
                    ResourceUploadTaskStatus.UPLOADING,
                    ResourceUploadTaskStatus.FINALIZING
                ),
                50,
                100,
                new Date()
            )
        );

        ResourceUploadTask updated = reload(task);
        assertEquals(50, updated.getBytesTransferred());
        assertEquals(100, updated.getTotalBytes());

        repository.requestCancellation(
            task.getId(),
            ResourceUploadTaskStatus.UPLOADING,
            ResourceUploadTaskStatus.CANCELLING
        );

        assertEquals(
            0,
            repository.updateProgress(
                task.getId(),
                "worker",
                Arrays.asList(
                    ResourceUploadTaskStatus.UPLOADING,
                    ResourceUploadTaskStatus.FINALIZING
                ),
                75,
                100,
                new Date()
            )
        );
    }

    @Test
    public void pendingHeartbeatRequiresPendingStatus() {
        ResourceUploadTask task = saveTask("worker");
        Date heartbeat = new Date();

        assertEquals(
            1,
            repository.updateHeartbeat(
                task.getId(),
                "worker",
                ResourceUploadTaskStatus.PENDING,
                heartbeat
            )
        );

        repository.startTask(
            task.getId(),
            "worker",
            ResourceUploadTaskStatus.PENDING,
            ResourceUploadTaskStatus.UPLOADING,
            new Date()
        );

        assertEquals(
            0,
            repository.updateHeartbeat(
                task.getId(),
                "worker",
                ResourceUploadTaskStatus.PENDING,
                new Date()
            )
        );
    }

    @Test
    public void statusLookupRequiresOwningWorker() {
        ResourceUploadTask task = saveTask("worker");

        assertEquals(
            Optional.of(ResourceUploadTaskStatus.PENDING),
            repository.findStatusByIdAndWorkerId(
                task.getId(),
                "worker"
            )
        );

        assertEquals(
            Optional.empty(),
            repository.findStatusByIdAndWorkerId(
                task.getId(),
                "another-worker"
            )
        );
    }

    @Test
    public void uniqueClaimPreventsConcurrentFilenameUpload() {
        ResourceUploadTask first = saveTask("worker-1");
        ResourceUploadTask second = saveTask("worker-2");

        start(first, "worker-1");
        start(second, "worker-2");

        String claimKey = ResourceUploadTask.activeClaimKey(
            "metadata",
            "file.zip"
        );

        assertEquals(
            1,
            repository.acquireClaim(
                first.getId(),
                "worker-1",
                ResourceUploadTaskStatus.UPLOADING,
                claimKey,
                new Date()
            )
        );

        assertThrows(
            DataIntegrityViolationException.class,
            () -> repository.acquireClaim(
                second.getId(),
                "worker-2",
                ResourceUploadTaskStatus.UPLOADING,
                claimKey,
                new Date()
            )
        );
    }

    @Test
    public void finalizingTaskCanBeCompletedAtomically() {
        ResourceUploadTask task = saveTask("worker");

        start(task, "worker");

        repository.startFinalizing(
            task.getId(),
            "worker",
            ResourceUploadTaskStatus.UPLOADING,
            ResourceUploadTaskStatus.FINALIZING,
            100,
            100,
            new Date()
        );

        assertEquals(
            1,
            repository.completeTask(
                task.getId(),
                "worker",
                ResourceUploadTaskStatus.FINALIZING,
                ResourceUploadTaskStatus.COMPLETED,
                "file.zip",
                100,
                100,
                new Date(),
                ResourceUploadTask.releasedClaimKey(task.getId())
            )
        );

        ResourceUploadTask completed = reload(task);

        assertEquals(
            ResourceUploadTaskStatus.COMPLETED,
            completed.getStatus()
        );
        assertEquals("file.zip", completed.getFilename());
        assertEquals(100, completed.getBytesTransferred());
        assertEquals(100, completed.getTotalBytes());
        assertNotNull(completed.getEndedDateTime());
        assertEquals(
            ResourceUploadTask.releasedClaimKey(task.getId()),
            completed.getClaimKey()
        );
    }

    @Test
    public void pendingTaskCanBeCancelledImmediately() {
        ResourceUploadTask task = saveTask("worker");

        assertEquals(
            1,
            repository.cancelPendingTask(
                task.getId(),
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.CANCELLED,
                new Date(),
                ResourceUploadTask.releasedClaimKey(task.getId())
            )
        );

        assertEquals(
            ResourceUploadTaskStatus.CANCELLED,
            reload(task).getStatus()
        );
    }

    @Test
    public void staleActiveTasksAreFailedAndClaimsReleased() {
        ResourceUploadTask task = saveTask("worker");
        Date oldHeartbeat =
            new Date(System.currentTimeMillis() - 60_000);

        repository.updateHeartbeat(
            task.getId(),
            "worker",
            ResourceUploadTaskStatus.PENDING,
            oldHeartbeat
        );

        assertEquals(
            1,
            repository.failStaleTasks(
                ResourceUploadTaskStatus.getActiveStatuses(),
                ResourceUploadTaskStatus.FAILED,
                "worker unavailable",
                new Date(),
                new Date(System.currentTimeMillis() - 30_000),
                "task:"
            )
        );

        ResourceUploadTask failed = reload(task);

        assertEquals(ResourceUploadTaskStatus.FAILED, failed.getStatus());
        assertEquals("worker unavailable", failed.getError());
        assertEquals(
            ResourceUploadTask.releasedClaimKey(task.getId()),
            failed.getClaimKey()
        );
    }

    @Test
    public void expiredTerminalTasksAreDeleted() {
        ResourceUploadTask task = saveTask("worker");
        Date oldDate = new Date(System.currentTimeMillis() - 60_000);

        repository.cancelPendingTask(
            task.getId(),
            ResourceUploadTaskStatus.PENDING,
            ResourceUploadTaskStatus.CANCELLED,
            oldDate,
            ResourceUploadTask.releasedClaimKey(task.getId())
        );

        assertEquals(
            1,
            repository.deleteExpiredTasks(
                ResourceUploadTaskStatus.getTerminalStatuses(),
                new Date(System.currentTimeMillis() - 30_000)
            )
        );

        assertEquals(
            Optional.empty(),
            repository.findById(task.getId())
        );
    }

    private ResourceUploadTask saveTask(String workerId) {
        return repository.save(
            ResourceUploadTask.create(
                "metadata",
                42,
                "https://example.org/file.zip",
                MetadataResourceVisibility.PUBLIC,
                false,
                workerId
            )
        );
    }

    private void start(ResourceUploadTask task, String workerId) {
        repository.startTask(
            task.getId(),
            workerId,
            ResourceUploadTaskStatus.PENDING,
            ResourceUploadTaskStatus.UPLOADING,
            new Date()
        );
    }

    private ResourceUploadTask reload(ResourceUploadTask task) {
        return repository.findById(task.getId()).orElseThrow(
            () -> new AssertionError("Task was not found")
        );
    }
}
