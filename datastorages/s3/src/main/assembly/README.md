# Amazon S3 data storage provider library

To install the Amazon S3 data storage provider in GeoNetwork:

1. Copy the `lib` folder jar files into to `{GEONETWORK_DIR}/WEB-INF/lib` folder.

2. Define the following environmental variables:

  - Use S3 data storage provider:

    ```shell
    export GEONETWORK_STORE_TYPE=s3
    ```

  - Setup S3 connection as described in https://geonetwork-opensource.org/manuals/4.0.x/en/install-guide/customizing-data-directory.html#using-a-s3-object-storage

3. Start GeoNetwork
