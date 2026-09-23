# Uploading attachments {#associating_resources_filestore}

!!! info "Version Added"

    3.2


If documents are not available, editors can upload attachments to a metadata record. The attachment is added to the filestore. The filestore can contain any kind of files.

![](img/filestore.png)

To upload a file, click the button and choose a file or drag&drop a file on the button. Files are stored in a folder in the data directory (see [Customizing the data directory](../../install-guide/customizing-data-directory.md)). There is one folder per metadata containing:

-   `public` folder with files accessible to all users
-   `private` folder with files accessible to identified user with download privilege (see [Managing privileges](../publishing/managing-privileges.md))

From the filestore:

-   click the file name to set the URL for the current document to attach
-   click the eye icon to view the document
-   click the locker to change the document visibility
-   click the cross to remove the file.

A file uploaded in this way will be exported in the metadata export file (MEF). Therefore, its URL will not be automatically added to the metadata. The URL is added when attaching the document to a specific element in the metadata (eg. overview, quality report, legend).

## Filestore configuration

By default, the maximum attachment size is 100 MB. The value is specified in bytes using the `api.params.maxUploadSize` property.

For example, the following Java system property configures a maximum attachment size of 1 GB:

```shell
-Dapi.params.maxUploadSize=1000000000
```

Files with a declared size larger than this limit are rejected before downloading. If the remote server does not provide a size, GeoNetwork enforces the limit while streaming the file.

When an attachment is downloaded from a remote URL, GeoNetwork applies connection and read timeouts. Both values are specified in milliseconds and default to 10 seconds:

```shell
-Dapi.params.uploadConnectTimeout=10000
-Dapi.params.uploadReadTimeout=10000
```

The connection timeout limits how long GeoNetwork waits to establish a connection to the remote server. The read timeout limits how long an individual read operation may wait for data. It does not limit the total duration of the download.

Types of attachments allowed to be uploaded can be configured in the system settings. See [Metadata configuration](../../administrator-guide/configuring-the-catalog/system-configuration.md#metadata_configuration) for more information.

## Uploading a resource from a URL asynchronously

!!! info "Version Added"

    4.4.13

The existing synchronous endpoint downloads and stores the remote resource before returning:

```http
PUT .../api/records/{metadataUuid}/attachments?url=https://example.org/file.zip
```

For large resources, use the asynchronous upload-task endpoint instead:

```http
POST .../api/records/{metadataUuid}/attachments/upload/tasks?url=https://example.org/file.zip
```

The following optional query parameters are also supported:

| Parameter    | Default   | Description |
| ------------ | --------- | ----------- |
| `visibility` | `public`  | Sharing policy for the stored attachment: `public` or `private`. |
| `approved`   | `false`   | Whether the approved metadata version should be used. |

The request returns `202 Accepted` with a persistent upload-task description:

```json
{
  "approved": false,
  "bytesTransferred": 0,
  "endedDateTime": null,
  "error": null,
  "filename": null,
  "id": "5f2c5b6e-2187-4bcd-8843-41e575a5ef56",
  "metadataUuid": "43d7c186-2187-4bcd-8843-41e575a5ef56",
  "startedDateTime": null,
  "status": "PENDING",
  "submittedDateTime": "2026-09-16T18:00:00.000+00:00",
  "totalBytes": -1,
  "url": "https://example.org/file.zip",
  "visibility": "PUBLIC"
}
```

The task may already be `UPLOADING` when the response is received if a worker was immediately available.

### Polling upload status

Retrieve an individual upload task using:

```http
GET .../api/records/{metadataUuid}/attachments/upload/tasks/{taskId}
```

List the upload tasks visible to the current user for a metadata record using:

```http
GET .../api/records/{metadataUuid}/attachments/upload/tasks
```

Continue polling until the task reaches one of the terminal states:

- `COMPLETED`
- `FAILED`
- `CANCELLED`

The client can calculate progress when `totalBytes` is greater than zero:

```text
percentComplete = bytesTransferred * 100 / totalBytes
```

If the remote server does not provide a content length, `totalBytes` remains `-1` and percentage progress cannot be calculated. The client should display indeterminate progress in that case.

Progress is collected continuously by the worker but persisted to the database at a configurable interval. A polling response may therefore temporarily show an older byte count.

### Cancelling an upload

Request cancellation using:

```http
DELETE .../api/records/{metadataUuid}/attachments/upload/tasks/{taskId}
```

A successful cancellation request returns `200 OK`.

Cancellation is accepted while a task is `PENDING`, `UPLOADING`, or already `CANCELLING`. Cancellation is rejected with `409 Conflict` after the task begins `FINALIZING` or reaches a terminal state.

Cancellation of a running task is cooperative. After requesting cancellation, continue polling until the task reaches `CANCELLED`. The worker checks the persisted task state during periodic synchronization, so cancellation state is not held only in application memory.

### Task statuses

| Status       | Meaning |
| ------------ | ------- |
| `PENDING`    | The task is waiting for an upload worker. |
| `UPLOADING`  | The remote resource is being downloaded and stored. |
| `FINALIZING` | The attachment transaction is being finalized. Cancellation is no longer accepted. |
| `CANCELLING` | Cancellation was requested and the worker is still stopping. |
| `COMPLETED`  | The attachment was stored successfully. |
| `FAILED`     | The upload failed. The `error` property contains the failure message. |
| `CANCELLED`  | The worker stopped after cancellation. |

The resolved `filename` is stored as soon as it becomes known. It may therefore be available on `FAILED` or `CANCELLED` tasks as well as completed tasks.

A byte count equal to `totalBytes` does not mean the task is complete. The task may still be `FINALIZING`; clients should use `status` to determine completion.

### Permissions

Creating, viewing, listing, and cancelling upload tasks requires edit access to the associated metadata record.

A task is visible to:

- the user who submitted it; or
- an Administrator.

Administrators can list and manage upload tasks submitted by other users.

### Duplicate uploads

GeoNetwork prevents concurrent asynchronous uploads with the same resolved filename for the same metadata record.

The filename claim is stored in the database rather than application memory. A task can initially be accepted and later become `FAILED` if its resolved filename conflicts with another active upload.

Synchronous URL uploads also check for an active asynchronous filename claim before storing the attachment.

### Task persistence and recovery

Upload-task state and filename claims are stored in the database rather than application memory.

The download itself, including its active network stream and worker future, remains local to the application process that accepted the task. The worker updates its heartbeat while the task remains non-terminal, including while a cancellation is in progress. If that process becomes unavailable, a later task-state synchronization marks the task as `FAILED` after its heartbeat becomes stale. The upload is not automatically resumed.

### Asynchronous upload configuration

The asynchronous worker pool and task-state synchronization can be configured using the following properties:

```properties
api.params.uploadTaskCorePoolSize=8
api.params.uploadTaskMaxPoolSize=8
api.params.uploadTaskQueueCapacity=50
api.params.uploadTaskKeepAliveSeconds=60
api.params.uploadTaskUpdateInterval=5000
api.params.uploadTaskStaleTimeout=60000
api.params.uploadTaskRetention=1800000
```

| Property | Unit | Description |
| -------- | ---- | ----------- |
| `api.params.uploadTaskCorePoolSize` | workers | Number of upload workers normally retained by each application instance. |
| `api.params.uploadTaskMaxPoolSize` | workers | Maximum number of upload workers in each application instance. |
| `api.params.uploadTaskQueueCapacity` | tasks | Number of tasks that may wait for a worker in each application instance. |
| `api.params.uploadTaskKeepAliveSeconds` | seconds | How long worker threads above the core pool size may remain idle. This has no effect when the core and maximum pool sizes are equal. |
| `api.params.uploadTaskUpdateInterval` | milliseconds | Interval between persisted progress/heartbeat updates and cancellation-state checks. |
| `api.params.uploadTaskStaleTimeout` | milliseconds | Heartbeat age after which an active task is eligible to be marked as failed. |
| `api.params.uploadTaskRetention` | milliseconds | How long terminal tasks are retained before becoming eligible for deletion. |

With the default worker configuration, each application instance can execute up to eight uploads concurrently and queue up to 50 additional tasks.
Further submissions are rejected and recorded as failed tasks.

The task stores and returns the source URL without user information, query parameters, or fragments because temporary download URLs
commonly contain credentials in those components. The worker still uses the complete URL supplied to the request.

Stale-task detection runs every update interval. Terminal-task cleanup runs every 60 update intervals, so terminal tasks may remain stored
by up to that additional cleanup interval after reaching the configured retention threshold.
