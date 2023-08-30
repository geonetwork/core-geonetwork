CMIS data storage provider library
----------------------------------

1) Copy the provided jar files to geonetwork/WEB-INF/lib folder

2) Define the following environmental variables

Use CMIS data storage provider:

export GEONETWORK_STORE_TYPE=cmis

Setup CMIS connection (check with your setup):

export CMIS_REPOSITORY_ID=-default-
export CMIS_USERNAME=username
export CMIS_PASSWORD=password
export CMIS_SERVICES_BASE_URL=http://localhost:8080/alfresco
export CMIS_BASE_REPOSITORY_PATH=geonetwork
export CMIS_BINDING_TYPE=browser
export CMIS_BROWSER_URL=/api/-default-/public/cmis/versions/1.1/browser

3) Start GeoNetwork
