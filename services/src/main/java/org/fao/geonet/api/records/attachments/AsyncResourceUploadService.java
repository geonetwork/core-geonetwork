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
import jeeves.transaction.TransactionManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.history.AttachmentAddedEvent;
import org.fao.geonet.repository.ResourceUploadTaskRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Runs asynchronous resource uploads and synchronizes their state with the
 * database.
 */
@Service
public class AsyncResourceUploadService implements DisposableBean {
    private static final int CLEANUP_INTERVAL_CYCLES = 60;

    @Autowired
    private ResourceUploadTaskRepository taskRepository;

    @Autowired
    private ServiceManager serviceManager;

    @Value("${api.params.uploadTaskUpdateInterval:5000}")
    private long taskUpdateInterval;

    @Value("${api.params.uploadTaskStaleTimeout:60000}")
    private long taskStaleTimeout;

    @Value("${api.params.uploadTaskRetention:1800000}")
    private long taskRetention;

    @Autowired
    @Qualifier("resourceUploadExecutor")
    private ThreadPoolTaskExecutor executor;

    private final String workerId = UUID.randomUUID().toString();

    private final ConcurrentMap<String, ResourceUploadExecution> executions =
        new ConcurrentHashMap<>();

    private final ScheduledExecutorService stateSynchronizer =
        Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("resource-upload-state-")
        );

    private final AtomicInteger synchronizationCycles = new AtomicInteger();

    /**
     * Starts the periodic task that persists worker progress and heartbeats,
     * detects cancellation changes in persistent task state, and periodically
     * cleans stale and expired tasks.
     */
    @PostConstruct
    public void startStateSynchronizer() {
        stateSynchronizer.scheduleWithFixedDelay(
            this::synchronizeTaskState,
            taskUpdateInterval,
            taskUpdateInterval,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Persists and schedules an asynchronous resource upload after the current
     * transaction commits.
     *
     * @param store attachment store used by the worker
     * @param requestContext request service context copied for worker execution
     * @param metadataUuid UUID of the metadata record receiving the resource
     * @param url remote resource URL
     * @param visibility visibility assigned to the stored resource
     * @param approved whether the approved metadata version should be used
     * @return the newly persisted pending task
     * @throws IllegalStateException if no authenticated user is available
     */
    public ResourceUploadTask submit(
        Store store,
        ServiceContext requestContext,
        String metadataUuid,
        URL url,
        MetadataResourceVisibility visibility,
        Boolean approved
    ) {
        UserSession userSession = requestContext.getUserSession();

        if (userSession == null) {
            throw new IllegalStateException(
                "An authenticated user is required to submit an upload."
            );
        }

        ResourceUploadTask task = ResourceUploadTask.create(
            metadataUuid,
            userSession.getUserIdAsInt(),
            sanitizeSourceUrl(url),
            visibility,
            Boolean.TRUE.equals(approved),
            workerId
        );

        taskRepository.save(task);

        ResourceUploadExecution execution =
            new ResourceUploadExecution(
                task.getId(),
                metadataUuid,
                workerId,
                taskRepository
            );

        String language = requestContext.getLanguage();
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(SecurityContextHolder.getContext().getAuthentication());

        FutureTask<Void> uploadFuture = new FutureTask<>(() -> {
            run(
                store,
                task.getId(),
                execution,
                url,
                userSession,
                securityContext,
                language,
                metadataUuid,
                visibility,
                approved
            );
            return null;
        });

        execution.setFuture(uploadFuture);

        executeAfterCommit(
            task.getId(),
            execution,
            uploadFuture
        );

        return getTask(task.getId());
    }

    private void run(
        Store store,
        String taskId,
        ResourceUploadExecution execution,
        URL url,
        UserSession userSession,
        SecurityContext securityContext,
        String language,
        String metadataUuid,
        MetadataResourceVisibility visibility,
        Boolean approved
    ) {
        try {
            SecurityContextHolder.setContext(securityContext);

            ConfigurableApplicationContext appContext =
                ApplicationContextHolder.get();

            int started = taskRepository.startTask(
                taskId,
                workerId,
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.UPLOADING,
                new Date()
            );

            if (started == 0) {
                throw new IllegalStateException(
                    "Upload task " + taskId + " could not transition from PENDING to UPLOADING."
                );
            }

            execution.markStarted();

            ServiceContext context = serviceManager.createServiceContext(
                "attachmentAsyncUpload",
                appContext
            );
            context.setLanguage(language == null ? "eng" : language);
            context.setUserSession(userSession);
            context.setAsThreadLocal();

            AtomicInteger transactionOutcome = new AtomicInteger(
                TransactionSynchronization.STATUS_UNKNOWN
            );

            MetadataResource resource =
                TransactionManager.runInTransaction(
                    "AsyncResourceUpload-" + taskId,
                    appContext,
                    TransactionManager.TransactionRequirement.CREATE_NEW,
                    TransactionManager.CommitBehavior.ALWAYS_COMMIT,
                    false,
                    status -> {
                        TransactionSynchronizationManager
                            .registerSynchronization(
                                new TransactionSynchronization() {
                                    @Override
                                    public void afterCompletion(int status) {
                                        transactionOutcome.set(status);
                                    }
                                }
                            );

                        MetadataResource uploaded = store.putResource(
                            context,
                            metadataUuid,
                            url,
                            visibility,
                            approved,
                            execution
                        );

                        if (execution.isCancelled()) {
                            throw new CancellationException(
                                "Resource upload task " + taskId +
                                    " was cancelled."
                            );
                        }

                        int finalizing = taskRepository.startFinalizing(
                            taskId,
                            workerId,
                            ResourceUploadTaskStatus.UPLOADING,
                            ResourceUploadTaskStatus.FINALIZING,
                            execution.getBytesTransferred(),
                            execution.getTotalBytes(),
                            new Date()
                        );

                        if (finalizing == 0) {
                            throw new CancellationException(
                                "Resource upload task " + taskId +
                                    " could not enter finalizing state."
                            );
                        }

                        String metadataIdString =
                            ApiUtils.getInternalId(
                                metadataUuid,
                                approved
                            );

                        if (metadataIdString != null) {
                            long metadataId =
                                Long.parseLong(metadataIdString);

                            new AttachmentAddedEvent(
                                metadataId,
                                userSession.getUserIdAsInt(),
                                uploaded.getFilename()
                            ).publish(appContext);
                        }

                        int completed = taskRepository.completeTask(
                            taskId,
                            workerId,
                            ResourceUploadTaskStatus.FINALIZING,
                            ResourceUploadTaskStatus.COMPLETED,
                            uploaded.getFilename(),
                            execution.getBytesTransferred(),
                            execution.getTotalBytes(),
                            new Date(),
                            ResourceUploadTask.releasedClaimKey(taskId)
                        );

                        if (completed == 0) {
                            throw new IllegalStateException(
                                "Resource upload task " + taskId +
                                    " could not be completed."
                            );
                        }

                        return uploaded;
                    }
                );

            if (transactionOutcome.get()
                != TransactionSynchronization.STATUS_COMMITTED) {
                throw new IllegalStateException(
                    "The upload transaction did not commit. " +
                        "Check the server logs for details."
                );
            }

            if (Log.isDebugEnabled(Geonet.RESOURCES)) {
                Log.debug(
                    Geonet.RESOURCES,
                    "Completed async upload task " + taskId +
                        " with stored file '" +
                        resource.getFilename() + "'."
                );
            }
        } catch (CancellationException e) {
            execution.cancel();
        } catch (Exception e) {
            if (!execution.isCancelled() && !execution.isFailed()) {
                Log.error(
                    Geonet.RESOURCES,
                    "Error uploading resource for record '" +
                        metadataUuid + "' (task " + taskId + ")",
                    e
                );

                failTask(
                    taskId,
                    execution,
                    e.getMessage() != null
                        ? e.getMessage()
                        : e.toString()
                );
            }
        } finally {
            if (execution.isCancelled()) {
                taskRepository.markCancelled(
                    taskId,
                    workerId,
                    ResourceUploadTaskStatus.CANCELLING,
                    ResourceUploadTaskStatus.CANCELLED,
                    execution.getBytesTransferred(),
                    execution.getTotalBytes(),
                    new Date(),
                    ResourceUploadTask.releasedClaimKey(taskId)
                );
            }

            executions.remove(taskId);
            SecurityContextHolder.clearContext();
        }
    }

    private void synchronizeTaskState() {
        try {
            ConfigurableApplicationContext appContext =
                ApplicationContextHolder.get();

            Set<String> taskIdsToCancel =
                TransactionManager.runInTransaction(
                    "SynchronizeResourceUploadTasks",
                    appContext,
                    TransactionManager.TransactionRequirement.CREATE_NEW,
                    TransactionManager.CommitBehavior.ALWAYS_COMMIT,
                    false,
                    status -> {
                        Date now = new Date();
                        Set<String> inactiveTaskIds = new HashSet<>();

                        for (
                            ResourceUploadExecution execution
                            : executions.values()
                        ) {
                            int updated;

                            if (execution.hasStarted()) {
                                updated = taskRepository.updateProgress(
                                    execution.getTaskId(),
                                    workerId,
                                    ResourceUploadTaskStatus.getNonTerminalStatuses(),
                                    execution.getBytesTransferred(),
                                    execution.getTotalBytes(),
                                    now
                                );
                            } else {
                                updated = taskRepository.updateHeartbeat(
                                    execution.getTaskId(),
                                    workerId,
                                    ResourceUploadTaskStatus.PENDING,
                                    now
                                );
                            }

                            if (
                                updated == 0
                                    && shouldCancelLocalExecution(
                                        execution.getTaskId()
                                    )
                            ) {
                                inactiveTaskIds.add(
                                    execution.getTaskId()
                                );
                            }
                        }

                        failStaleTasks(now);

                        if (
                            synchronizationCycles.incrementAndGet()
                                % CLEANUP_INTERVAL_CYCLES == 0
                        ) {
                            deleteExpiredTasks(now);
                        }

                        inactiveTaskIds.addAll(
                            taskRepository.findIdsByWorkerIdAndStatus(
                                workerId,
                                ResourceUploadTaskStatus.CANCELLING
                            )
                        );

                        return inactiveTaskIds;
                    }
                );

            taskIdsToCancel.forEach(this::cancelLocalExecution);
        } catch (Exception e) {
            Log.error(
                Geonet.RESOURCES,
                "Error synchronizing resource upload task state.",
                e
            );
        }
    }

    private boolean shouldCancelLocalExecution(String taskId) {
        Optional<ResourceUploadTaskStatus> status =
            taskRepository.findStatusByIdAndWorkerId(
                taskId,
                workerId
            );

        return !status.isPresent()
            || status.get() == ResourceUploadTaskStatus.CANCELLING
            || status.get().isTerminal();
    }

    private void failStaleTasks(Date now) {
        Date staleCutoff = new Date(
            now.getTime() - taskStaleTimeout
        );

        taskRepository.failStaleTasks(
            ResourceUploadTaskStatus.getNonTerminalStatuses(),
            ResourceUploadTaskStatus.FAILED,
            "The upload stopped because the server processing it " +
                "is no longer available.",
            now,
            staleCutoff,
            "task:"
        );
    }

    private void deleteExpiredTasks(Date now) {
        Date retentionCutoff = new Date(
            now.getTime() - taskRetention
        );

        taskRepository.deleteExpiredTasks(
            ResourceUploadTaskStatus.getTerminalStatuses(),
            retentionCutoff
        );
    }

    private void failTask(
        String taskId,
        ResourceUploadExecution execution,
        String error
    ) {
        taskRepository.failTask(
            taskId,
            workerId,
            ResourceUploadTaskStatus.getFailableStatuses(),
            ResourceUploadTaskStatus.FAILED,
            error,
            execution.getBytesTransferred(),
            execution.getTotalBytes(),
            new Date(),
            ResourceUploadTask.releasedClaimKey(taskId)
        );
    }

    /**
     * Requests cancellation after verifying that the caller owns the task or is
     * an administrator.
     *
     * @param metadataUuid UUID of the task's metadata record
     * @param taskId upload task identifier
     * @param request current HTTP request
     * @return {@code true} when cancellation was accepted
     * @throws Exception if access validation or task lookup fails
     */
    public boolean cancel(
        String metadataUuid,
        String taskId,
        HttpServletRequest request
    ) throws Exception {
        ResourceUploadTask task = getOwnedTaskOrThrow(
            metadataUuid,
            taskId,
            request
        );

        return cancel(task);
    }

    /**
     * Requests cancellation using an atomic state transition.
     *
     * <p>Pending tasks become cancelled immediately. Uploading tasks enter
     * {@link ResourceUploadTaskStatus#CANCELLING} for cooperative worker
     * shutdown. Finalizing and terminal tasks cannot be cancelled.
     *
     * @param task current persistent task state
     * @return {@code true} when cancellation was accepted
     */
    public boolean cancel(ResourceUploadTask task) {
        Date now = new Date();
        int updated;

        if (task.getStatus() == ResourceUploadTaskStatus.PENDING) {
            updated = taskRepository.cancelPendingTask(
                task.getId(),
                ResourceUploadTaskStatus.PENDING,
                ResourceUploadTaskStatus.CANCELLED,
                now,
                ResourceUploadTask.releasedClaimKey(task.getId())
            );
        } else if (
            task.getStatus() == ResourceUploadTaskStatus.UPLOADING
        ) {
            updated = taskRepository.requestCancellation(
                task.getId(),
                ResourceUploadTaskStatus.UPLOADING,
                ResourceUploadTaskStatus.CANCELLING
            );
        } else if (
            task.getStatus() == ResourceUploadTaskStatus.CANCELLING
        ) {
            updated = 1;
        } else {
            return false;
        }

        if (updated == 0) {
            return false;
        }

        cancelLocalExecution(task.getId());
        return true;
    }

    private void cancelLocalExecution(String taskId) {
        ResourceUploadExecution execution = executions.get(taskId);

        if (execution == null) {
            return;
        }

        execution.cancel();

        FutureTask<Void> uploadFuture = execution.getFuture();
        if (uploadFuture == null) {
            return;
        }

        boolean removedFromQueue =
            executor.getThreadPoolExecutor().remove(uploadFuture);

        if (removedFromQueue || !execution.hasStarted()) {
            executions.remove(taskId, execution);
        }
    }

    /**
     * Lists tasks visible to a user for one metadata record.
     *
     * <p>Administrators receive all tasks; other authenticated users receive
     * only tasks they submitted.
     *
     * @param metadataUuid metadata record UUID
     * @param userSession current user session
     * @return visible tasks ordered by submission time descending, or an empty
     *         list when no user session is available
     */
    public List<ResourceUploadTask> listUploadsForUser(
        String metadataUuid,
        UserSession userSession
    ) {
        if (userSession == null) {
            return Collections.emptyList();
        }

        if (Profile.Administrator.equals(userSession.getProfile())) {
            return taskRepository
                .findByMetadataUuidOrderBySubmittedDateTimeDesc(
                    metadataUuid
                );
        }

        return taskRepository
            .findByMetadataUuidAndOwnerUserIdOrderBySubmittedDateTimeDesc(
                metadataUuid,
                userSession.getUserIdAsInt()
            );
    }

    /**
     * Loads a task and verifies that the current user owns it or is an
     * administrator.
     *
     * @param metadataUuid metadata record UUID
     * @param taskId upload task identifier
     * @param request current HTTP request
     * @return the authorized task
     * @throws ResourceNotFoundException if the task does not exist for the record
     * @throws SecurityException if the current user may not access the task
     * @throws Exception if user-session access fails
     */
    public ResourceUploadTask getOwnedTaskOrThrow(
        String metadataUuid,
        String taskId,
        HttpServletRequest request
    ) throws Exception {
        ResourceUploadTask task = taskRepository
            .findByIdAndMetadataUuid(taskId, metadataUuid)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format(
                    "Upload task '%s' not found for record '%s'.",
                    taskId,
                    metadataUuid
                )
            ));

        UserSession userSession = ApiUtils.getUserSession(
            request.getSession()
        );

        if (!isTaskOwnerOrAdmin(task, userSession)) {
            throw new SecurityException(
                String.format(
                    "User '%s' is not allowed to access upload task '%s'.",
                    userSession != null
                        ? userSession.getUsername()
                        : "unknown",
                    taskId
                )
            );
        }

        return task;
    }

    private ResourceUploadTask getTask(String taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalStateException(
                "Resource upload task '" + taskId + "' was not found."
            ));
    }

    private boolean isTaskOwnerOrAdmin(
        ResourceUploadTask task,
        UserSession userSession
    ) {
        if (userSession == null) {
            return false;
        }

        if (Profile.Administrator.equals(userSession.getProfile())) {
            return true;
        }

        return task.getOwnerUserId().equals(
            userSession.getUserIdAsInt()
        );
    }

    /**
     * Removes query parameters, fragments, and user information before a
     * source URL is persisted or returned through the task API. Temporary
     * download URLs commonly carry credentials in those components.
     *
     * @param sourceUrl source URL used by the in-memory upload execution
     * @return a credential-free representation suitable for persistence
     */
    private String sanitizeSourceUrl(URL sourceUrl) {
        try {
            URI sourceUri = sourceUrl.toURI();

            return new URI(
                sourceUri.getScheme(),
                null,
                sourceUri.getHost(),
                sourceUri.getPort(),
                sourceUri.getPath(),
                null,
                null
            ).toASCIIString();
        } catch (URISyntaxException e) {
            String port = sourceUrl.getPort() == -1
                ? ""
                : ":" + sourceUrl.getPort();

            return sourceUrl.getProtocol() + "://" +
                sourceUrl.getHost() + port + sourceUrl.getPath();
        }
    }

    /**
     * Submits an upload to the executor after the transaction that created its
     * database task has committed.
     *
     * <p>If there is no surrounding transaction, the repository save has already
     * committed and the upload can be submitted immediately.
     *
     * @param taskId identifier of the persistent upload task
     * @param execution local execution state for the upload
     * @param uploadFuture work that performs the upload
     */
    private void executeAfterCommit(
        String taskId,
        ResourceUploadExecution execution,
        FutureTask<Void> uploadFuture
    ) {
        if (
            TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()
        ) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        registerAndExecuteUpload(
                            taskId,
                            execution,
                            uploadFuture
                        );
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (
                            status
                                != TransactionSynchronization.STATUS_COMMITTED
                        ) {
                            executions.remove(taskId, execution);
                            uploadFuture.cancel(false);
                        }
                    }
                }
            );
        } else {
            registerAndExecuteUpload(
                taskId,
                execution,
                uploadFuture
            );
        }
    }

    /**
     * Makes an execution visible to the node-local synchronizer only after its
     * persistent task is committed, then submits it to the worker pool.
     */
    private void registerAndExecuteUpload(
        String taskId,
        ResourceUploadExecution execution,
        FutureTask<Void> uploadFuture
    ) {
        executions.put(taskId, execution);
        executeUpload(taskId, execution, uploadFuture);
    }

    /**
     * Submits an upload to the worker pool and records executor rejection.
     */
    private void executeUpload(
        String taskId,
        ResourceUploadExecution execution,
        FutureTask<Void> uploadFuture
    ) {
        try {
            executor.execute(uploadFuture);
        } catch (RejectedExecutionException e) {
            failTask(
                taskId,
                execution,
                "The server is too busy to process this upload right now. " +
                    "Please retry later."
            );

            executions.remove(taskId, execution);
            uploadFuture.cancel(false);
        }
    }

    /**
     * Stops the background state synchronizer during application shutdown.
     */
    @Override
    public void destroy() {
        stateSynchronizer.shutdownNow();

        awaitTermination(
            stateSynchronizer,
            "resource upload state synchronizer"
        );
    }

    private void awaitTermination(
        java.util.concurrent.ExecutorService service,
        String name
    ) {
        try {
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                Log.warning(
                    Geonet.RESOURCES,
                    "Timed out waiting for " + name + " to stop."
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(
                runnable,
                prefix + counter.getAndIncrement()
            );
            thread.setDaemon(true);
            return thread;
        }
    }
}
