# OpenDAL Data storage

Extension for data storage using [Apache OpenDAL](https://opendal.apache.org/), which provides a unified data access layer for various storage services.

## Configuration

The OpenDAL datastorage can be configured using environment variables. These variables are mapped to the OpenDAL operator configuration.

### Environment Variables

| Variable | Property | Default | Description |
|----------|----------|---------|-------------|
| `OPENDAL_SCHEME` | `opendal.scheme` | `fs` | The storage scheme (e.g., `fs`, `s3`, `azblob`, `gcs`). |
| `OPENDAL_ROOT` | `opendal.root` | `/tmp/opendal` | The root directory or path for the storage. |
| `OPENDAL_ENDPOINT` | `opendal.endpoint` | | The endpoint URL (required for S3-compatible storage). |
| `OPENDAL_BUCKET` | `opendal.bucket` | | The bucket name (required for S3/GCS/Azblob). |
| `OPENDAL_ACCESS_KEY_ID` | `opendal.access_key_id` | | Access key ID for authentication. |
| `OPENDAL_SECRET_ACCESS_KEY` | `opendal.secret_access_key` | | Secret access key for authentication. |
| `OPENDAL_REGION` | `opendal.region` | | The region for the storage service. |
| `OPENDAL_USERNAME` | `opendal.username` | | Username for authentication (e.g., WebDAV). |
| `OPENDAL_PASSWORD` | `opendal.password` | | Password for authentication (e.g., WebDAV). |

## Examples

### Local Filesystem

To configure OpenDAL to use the local filesystem:

```bash
export OPENDAL_SCHEME=fs
export OPENDAL_ROOT=/path/to/your/storage
```

### Amazon S3

To configure OpenDAL to use Amazon S3:

```bash
export OPENDAL_SCHEME=s3
export OPENDAL_ROOT=my-folder
export OPENDAL_BUCKET=my-bucket
export OPENDAL_REGION=us-east-1
export OPENDAL_ACCESS_KEY_ID=your_access_key
export OPENDAL_SECRET_ACCESS_KEY=your_secret_key
```

### S3 Compatible Storage (e.g., Minio)

```bash
export OPENDAL_SCHEME=s3
export OPENDAL_ENDPOINT=http://localhost:9000
export OPENDAL_BUCKET=my-bucket
export OPENDAL_ACCESS_KEY_ID=minioadmin
export OPENDAL_SECRET_ACCESS_KEY=minioadmin
```

### WebDAV

To configure OpenDAL to use a WebDAV server:

```bash
export OPENDAL_SCHEME=webdav
export OPENDAL_ENDPOINT=http://your-webdav-server.com/dav
export OPENDAL_ROOT=/remote.php/dav/files/user/
export OPENDAL_USERNAME=your_username
export OPENDAL_PASSWORD=your_password
```
