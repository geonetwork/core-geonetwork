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

package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing the status of a resource upload task.
 */
public enum ResourceUploadTaskStatus {
    /**
     * The task is pending and has not started yet.
     */
    PENDING(true, true, false, false),
    /**
     * The task is currently being uploaded.
     */
    UPLOADING(true, true, false, true),
    /**
     * The task is finalizing after the upload is complete.
     */
    FINALIZING(true, true, false, true),
    /**
     * The task is being cancelled.
     */
    CANCELLING(false, true, false, false),
    /**
     * The task has been completed successfully.
     */
    COMPLETED(false, false, true, false),
    /**
     * The task has failed.
     */
    FAILED(false, false, true, false),
    /**
     * The task has been cancelled.
     */
    CANCELLED(false, false, true, false);

    /**
     * Indicates whether the task can fail in this status.
     */
    private final boolean failable;

    /**
     * Indicates whether the task is active in this status.
     */
    private final boolean active;

    /**
     * Indicates whether the task is in a terminal status.
     */
    private final boolean terminal;

    /**
     * Indicates whether the task can provide progress updates in this status.
     */
    private final boolean progressUpdateAllowed;

    /**
     * Creates a task status with its supported state classifications.
     *
     * @param failable whether an execution failure may transition this status
     *                 to {@link #FAILED}
     * @param active whether the task is still active
     * @param terminal whether no further state transition is expected
     * @param progressUpdateAllowed whether a worker may persist progress and
     *                              heartbeat updates in this status
     */
    ResourceUploadTaskStatus(boolean failable, boolean active, boolean terminal, boolean progressUpdateAllowed) {
        this.failable = failable;
        this.active = active;
        this.terminal = terminal;
        this.progressUpdateAllowed = progressUpdateAllowed;
    }

    /**
     * Returns whether the task can fail in this status.
     *
     * @return true if the task can fail, false otherwise
     */
    public boolean isFailable() {
        return failable;
    }

    /**
     * Returns whether the task is still active.
     *
     * @return {@code true} for non-terminal task states
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns whether the task has finished.
     *
     * @return {@code true} for completed, failed, or cancelled tasks
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Returns whether worker progress and heartbeat updates are accepted in this
     * status.
     *
     * @return {@code true} while uploading or finalizing
     */
    public boolean isProgressUpdateAllowed() {
        return progressUpdateAllowed;
    }

    /**
     * Returns all statuses considered active for heartbeat and stale-task
     * processing.
     *
     * @return active task statuses in declaration order
     */
    public static List<ResourceUploadTaskStatus> getActiveStatuses() {
        List<ResourceUploadTaskStatus> activeStatuses = new ArrayList<>();
        for (ResourceUploadTaskStatus status : ResourceUploadTaskStatus.values()) {
            if (status.isActive()) {
                activeStatuses.add(status);
            }
        }
        return activeStatuses;
    }

    /**
     * Returns all statuses eligible for terminal-task retention cleanup.
     *
     * @return terminal task statuses in declaration order
     */
    public static List<ResourceUploadTaskStatus> getTerminalStatuses() {
        List<ResourceUploadTaskStatus> terminalStatuses = new ArrayList<>();
        for (ResourceUploadTaskStatus status : ResourceUploadTaskStatus.values()) {
            if (status.isTerminal()) {
                terminalStatuses.add(status);
            }
        }
        return terminalStatuses;
    }

    /**
     * Returns the statuses in which a worker may persist byte counts and a
     * heartbeat.
     *
     * @return progress-update statuses in declaration order
     */
    public static List<ResourceUploadTaskStatus> getProgressUpdateStatuses() {
        List<ResourceUploadTaskStatus> progressUpdateStatuses = new ArrayList<>();
        for (ResourceUploadTaskStatus status : ResourceUploadTaskStatus.values()) {
            if (status.isProgressUpdateAllowed()) {
                progressUpdateStatuses.add(status);
            }
        }
        return progressUpdateStatuses;
    }

    /**
     * Returns the statuses that may transition to {@link #FAILED} when worker
     * execution fails.
     *
     * @return failable statuses in declaration order
     */
    public static List<ResourceUploadTaskStatus> getFailableStatuses() {
        List<ResourceUploadTaskStatus> failableStatuses = new ArrayList<>();
        for (ResourceUploadTaskStatus status : ResourceUploadTaskStatus.values()) {
            if (status.isFailable()) {
                failableStatuses.add(status);
            }
        }
        return failableStatuses;
    }
}
