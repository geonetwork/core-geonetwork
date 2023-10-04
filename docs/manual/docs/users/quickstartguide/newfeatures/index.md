# New Features {#newfeatures}

The new GeoNetwork opensource comes with substantial upgrades of different components.

## 2.10 release

### Search

-   **Faceted search**: Narrow your search by easily selecting new filter
-   **Data Catalog Vocabulary and RDF services**: Increase discoverability and enable applications easily to consume metadata using W3C DCAT format
-   **Javascript widget user interface** : A 3rd flavour of home page based on HTML5 is also available

### Metadata

-   **Metadata on maps**: Add template for making metadata on static or interactive maps. Add a search criteria for easily found maps. Web Map Context could be loaded into the map viewer.
-   **Metadata linked data**: Easier metadata relation configuration and new support for source dataset and siblings. See [Link metadata and other resources](../new_metadata/linking.md))
-   **Hide part of metadata**: Provide a method to hide portions of metadata using withHeld ISO attribute
-   **Wiki markup in metadata**: Allow users to enter markup text in metadata elements and have results shown with rendered html
-   **WFS data downloader**: Simple component to download WFS data

### Administration

-   **User profile**: Setup user belonging to multiple groups with different profiles
-   **Virtual CSW** configuration interface to add new end points

### Others

-   **Security layer**: New security layer based on Spring Security adding support for CAS and much more flexible LDAP configuration
-   **Xlink**: Add local:// as a protocal for xlink links
-   Provide basic functionnalities (ie. search and view) when **database is in readonly**

## 2.8 release

### User interface

-   **Javascript widget user interface:** A new user interface using one of the latest Javascript widget libraries (extJS) has been added to support searching, editing and viewing metadata records. The user interface is now much easier for Javascript developers to reorganize and customize. GeoNetwork comes with two flavours of home page: one has the sidebar search similar to the old interface and the other uses a tabbed search layout. The 2.6.x user interface is still available as the default and has been updated.

![figure](Home_page_tn.png)
*New home page of GeoNetwork opensource using JavaScript Widgets - tab layout*

![figure](Home_page_n.png)
*New home page of GeoNetwork opensource using JavaScript Widgets- sidebar layout*

### Administration

-   **Search Statistics:** Captures and displays statistics on searches carried out in GeoNetwork. The statistics can be summarized in tables or in charts using JFreeChart. There is an extensible interface that you can use to display your own statistics. See [Search Statistics](../../features/search_statistics/index.md).
-   **New Harvesters:** OGC Harvesting: Sensor Observation Service, Z3950 harvesting, Web Accessible Folder (WAF), GeoPortal 9.3.x via REST API See [Harvesting](../../managing_metadata/harvesting/index.md).
-   **Harvest History and Scheduling:** Harvesting events are now recorded in the database for review at any time. See [Harvest History](../../managing_metadata/harvesting/index.md#harvest_history). Harvester scheduling is now much more flexible, you can start a harvest at any time of the day and at almost any interval (weekly etc).
-   **Extended Metadata Exchange Format (MEF):** More than one metadata file can be present in a MEF Zip archive. This is MEF version 2. See [Export facilities](../../managing_metadata/export/index.md).
-   **System Monitoring:** Automatically monitoring the health of a Geonetwork web application. See [System Monitoring](../../admin/monitoring/index.md).

### Metadata

-   **Metadata Status:** Allows finer control of the metadata workflow. Records can be assigned a status that reflects where they are in the metadata workflow: draft, approved, retired, submitted, rejected. When the status changes the relevant user is informed via email. eg. when an editor changes the status to 'submitted', the content reviewer receives an email requesting review. See [Status](../../managing_metadata/status/index.md).
-   **Metadata Versioning:** Captures changes to metadata records and metadata properties (status, privileges, categories) and records them as versions in a subversion respository. See [Versioning](../../managing_metadata/versioning/index.md).
-   **Publishing data to GeoServer from GeoNetwork:** You can now publish geospatial information in the form of GeoTIFF, shapefile or spatial table in a database to GeoServer from GeoNetwork. See [Publish uploaded data as WMS, WFS](../new_metadata/linking.md#GeoPublisher).
-   **Custom Metadata Formatters:** You can now create your own XSLT to format metadata to suit your needs, zip it up and plug it in to GeoNetwork. See [Formatter](../../managing_metadata/formatter/index.md).
-   **Assembling Metadata Records from Reusable Components:** Metadata records can now be assembled from reusable components (eg. contact information). The components can be present in the local catalog or brought in from a remote catalog (with caching to speed up access). A component directory interface is available for editing and viewing the components. See [Fragments](../../managing_metadata/fragments/index.md).
-   **Editor Improvements:** Picking terms from a thesaurus using a search widget, selecting reusable metadata components for inclusion in the record, user defined suggestions or picklists to control content, context sensitive help, creating relationships between records.
-   **Plug in metadata schemas:** You can define your own metadata schema and plug it into GeoNetwork on demand. Documentation to help you do this and example plug in schemas can be found in the Developers Manual. Some of the most common community plug in schemas can be downloaded from the GeoNetwork source code repository. See [Adding a schema](../../managing_metadata/schemas/index.md).
-   **Multilingual Indexing:** If you have to cope with metadata in different languages, GeoNetwork can now index each language and search all across language indexes by translating your search terms. See [Multilingual search](../../features/multilingual/index.md).
-   **Enhanced Thesaurus support:** Thesauri can be loaded from ISO19135 register records and SKOS files. Keywords in ISO records are anchored to the definition of the concept in the thesaurus. See [Thesaurus](../../features/thesaurus/index.md).

### CSW service

-   **Virtual CSW Endpoints:** Now you can define a custom CSW service that works with a set of metadata records that you define. See [Virtual CSW server entry points](../../admin/csw-configuration/index.md#VirtualCSW).

### INSPIRE Directive

-   **Support for the INSPIRE Directive:** Indexing and user interface extensions to support those who need to implement the INSPIRE metadata directive (EU).
-   **Installer package to enable INSPIRE options:** An optional new package in the installer enables GeoNetwork INSPIRE features if selected, avoiding manual steps to enable INSPIRE support.

### Other

-   **Improved Database Connection Handling and Pooling:** Replacement of the Jeeves based database connection pool with the widely used and more robust Apache Database Connection Pool (DBCP). Addition of JNDI or container based database connection support. See [Database configuration](../../admin/advanced-configuration/index.md#Database_JNDI_configuration).
-   **Configuration Overrides:** Now you can add your own configuration options to GeoNetwork, keep them in one file and maintain them independently from GeoNetwork. See [Configuration override](../../admin/advanced-configuration/index.md#adv_configuration_overriddes).
-   **Many other improvements:** charset detection and conversion on import, batch application of an XSLT to a selected set of metadata records (see [Processing](../../managing_metadata/processing/index.md)), remote notification of metadata changes, automatic integration tests to improve development and reduce regression and, of course, many bug fixes.
