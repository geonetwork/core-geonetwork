# Installation guide {#installation}

!!! info

    GeoNetwork requires a Java 8 environment (JRE) to be installed on your system. Later versions of Java will not work at present. This must be done prior to installation.

Before you can use GeoNetwork on your own computer, you need to install it:

1. GeoNetwork uses the Elasticsearch engine which must be installed first:

    * [Installing search platform](installing-index.md)

2. There are several different ways to install GeoNetwork:

    *   [Installing using a ZIP file](installing-from-zip.md)
    *   [Installing from WAR file](installing-from-war-file.md)
    *   [Installing with docker](installing-with-docker.md)
    *   [Building from Source Code](installing-from-source-code.md)

    !!! note
    
        A windows installer is also available from the GeoNetwork download page, [Installing a third-party distribution](https://geonetwork-opensource.org/downloads.html#third-party-distributions) .


3. After installing the application, you can configure the following:

    -   [Configuring the database](configuring-database.md)
    -   [Customizing the data directory](customizing-data-directory.md)
    -   [Logging](logging.md)
    -   [Configuring printing of the map](map-print-setup.md)
    -   [Loading templates and sample data](loading-samples.md)
