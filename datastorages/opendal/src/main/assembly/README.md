# OpenDAL data storage provider library

To install the OpenDAL data storage provider in GeoNetwork:

1. Copy the `lib` folder jar files into to `{GEONETWORK_DIR}/WEB-INF/lib` folder.

2. Define the following environmental variables:

    - Use OpenDAL data storage provider:

        ```bash
        export GEONETWORK_STORE_TYPE=opendal
        ```

    - Setup OpenDAL connection (check with your setup):

        - Filesystem example:

        ```bash
        export OPENDAL_SCHEME=fs
        export OPENDAL_ROOT=/tmp/opendal
        ```

        - S3 example:

        ```bash
        export OPENDAL_SCHEME=s3
        export OPENDAL_ROOT=/
        export OPENDAL_BUCKET=my-bucket
        export OPENDAL_ENDPOINT=https://s3.amazonaws.com
        export OPENDAL_REGION=us-east-1
        export OPENDAL_ACCESS_KEY_ID=access_key
        export OPENDAL_SECRET_ACCESS_KEY=secret_key
        ```

        - WebDAV example:

        ```bash
        export OPENDAL_SCHEME=webdav
        export OPENDAL_ENDPOINT=http://your-webdav-server.com/dav
        export OPENDAL_ROOT=/remote.php/dav/files/user/
        export OPENDAL_USERNAME=your_username
        export OPENDAL_PASSWORD=your_password
        ```
      
3. Start GeoNetwork
