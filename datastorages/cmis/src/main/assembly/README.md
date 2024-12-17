# CMIS data storage provider library

To install the CMIS data storage provider in GeoNetwork:

1. Copy the `lib` folder jar files into to `{GEONETWORK_DIR}/WEB-INF/lib` folder.

2. Define the following environmental variables:

    - Use CMIS data storage provider:

        ```bash
        export GEONETWORK_STORE_TYPE=cmis
        ```

    - Setup CMIS connection (check with your setup):

        ```bash
        export CMIS_REPOSITORY_ID=-default-
        export CMIS_USERNAME=username
        export CMIS_PASSWORD=password
        export CMIS_SERVICES_BASE_URL=http://localhost:8080/alfresco
        export CMIS_BASE_REPOSITORY_PATH=geonetwork
        export CMIS_BINDING_TYPE=browser
        export CMIS_BROWSER_URL=/api/-default-/public/cmis/versions/1.1/browser
        ```

3. Start GeoNetwork
