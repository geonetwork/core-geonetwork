# Updating the application

## Before you start

### Jasypt encryption settings

Since GeoNetwork 4.0.4, passwords stored in the database for the mail server, harvesters, etc. are encrypted with [Jasypt](https://www.jasypt.org/).
    
By default, a random encryption password is generated when GeoNetwork is started, if it is not already defined, and it is stored in the data directory **`data/config/encryptor/encryptor.properties`**.

=== "Application data directory"

    If you are using the application data direcotry, the encryption password is stored in the file **`/geonetwork/WEB-INF/data/config/encryptor/encryptor.properties`**. 
    
    !!! warning
    
        This **`encryptor.propertie`** file **must be copied** to the new installation when upgrading the application; otherwise, it will not be possible to decrypt the existing passwords stored in the database.

=== "External data directory"

    If you have set the location of the data directory outside of the application, the file will be stored in this external location at **`data/config/encryptor/encryptor.properties`**
  
    Read more at [Customizing the data directory](../../install-guide/customizing-data-directory.md).

## Updating from GeoNetwork 4.2 to GeoNetwork 4.4

Updating from GeoNetwork 4.2 to GeoNetwork 4.4 is a minor upgrade and may be performed in place.

Before you start:

=== "Application data directory"
    
    1. Stop the services:
        
        * Application server running GeoNetwork
    
    2. When using application data directory be sure to backup  **`/geonetwork/WEB-INF/data/config/encryptor/encryptor.properties`** 

=== "External data directory"

    Before you start:
    
    1. Stop the services:
        
        * Application server running GeoNetwork
Update:

=== "Application data directory"
    
    1. GeoNetwork 4.4 requires a Java 11 environment (JRE) to be installed on your system.
       
        Later versions of Java will not work at present. This must be done prior to installation.
       
    2. This release of GeoNetwork 4.4 is developed and tested with Elasticsearch `7.17.15`.
    
        If required update Elasticsearch, see [Installing search platform](../../install-guide/installing-index.md).
    
    3. Minor update may be performed in place, update **`geonetwork`** according to [installation approach](../../install-guide/index.md) used.
        
        !!! note
        
            Configurations changes made to application  **`/WEB-INF/`** files should be backed up, and the same configuration changes reapplied to the new application.
        
            With the service stopped you may find it easier to manually deploy (by unzipping the **`geonetwork.war`**
            into the correct **`webapps/geonetwork/`** location) in order to apply configuration changes to **`/WEB-INF/**`.
       
            Read more at [Configuration](../../install-guide/configuring-database.md).

    4. Restore the backup of **`encryptor.properties`** to the application data directory.

    5. Restart services:
        
        * Application server for GeoNetwork

=== "External data directory"

    1. GeoNetwork 4.4 requires a Java 11 environment (JRE) to be installed on your system.
       
        Later versions of Java will not work at present. This must be done prior to installation.

    2. This release of GeoNetwork 4.4 is developed and tested with Elasticsearch `7.17.15`.
    
        If required update Elasticsearch, see [Installing search platform](../../install-guide/installing-index.md).

    3. Minor update may be performed in place, update **`geonetwork`** according to [installation approach](../../install-guide/index.md) used.
       
        !!! note
        
            Configurations changes made to application  **`/WEB-INF/`** files should be backed up, and the same configuration changes reapplied to the new application.
        
            With the service stopped you may find it easier to manually deploy (by unzipping the **`geonetwork.war`**
            into the correct **`webapps/geonetwork/`** location) in order to apply configuration changes to **`/WEB-INF/**`.
       
            Read more at [Configuration](../../install-guide/configuring-database.md).

    4. Restart services:
        
        * Application server for GeoNetwork
        
Guidance:

* [Changelog 4.4.x](../../overview/change-log/latest/index.md)
* No additional guidance provided at this time.

## Upgrade from GeoNetwork 3.0 to GeoNetwork 4.4

Upgrading from GeoNetwork 3.0 to GeoNetwork 4.4 is a major upgrade.

We do not recommend updating a production system. Instead treat this as a migration, setting up a new GeoNetwork 4.0 installation and transfer data and settings.

Before you start:

* Backup your database

* Backup your data directory

GeoNetwork for Migration:

1. GeoNetwork 4.4 requires a Java 11 environment (JRE) to be installed on your system.
   
    Later versions of Java will not work at present. This must be done prior to installation.

2. GeoNetwork 4 changes the search engine to Elasticsearch.

    For installation instructions see [Installing search platform](../../install-guide/installing-index.md).

2. Perform a new install of GeoNetwork 4.0, according to [installation approach](../../install-guide/index.md) used.

3. Configure GeoNetwork use to your database and data directory.

4. Start services:
    
    * Database
    * Elasticsearch
    * Application server for GeoNetwork

4. GeoNetwork 4 encrypts stored passwords using Jasypt as described at the top of this page.
   
    During startup a random encryption password will be generated and stored in data directory **`data/config/encryptor/encryptor.properties`**.

5. During startup the database schema will be updated to match the table structure required for GeoNetwork 4.

6. GeoNetwork will create indices automatically when connecting to Elasticsearch.

Guidance:

* Be advised of the following changes in application functionality:

   * [Permalink to search page from GeoNetwork 3.x will need to be updated to work with 4.x. ](../../user-guide/quick-start/index.md#upgrading-from-geonetwork-3-guidance)

* Please be advised of the following changes to application services:
   * [Search / q service is replaced by Elasticsearch](../../api/q-search.md#upgrading-from-geonetwork-30-guidance)
   * [Virtual CSW end-points are replaced by sub-portals](../../api/csw.md#upgrading-from-geonetwork-30-guidance)
   * [RDF DCAT API no longer available](../../api/rdf-dcat.md#upgrading-from-geonetwork-30-guidance)
   * [Z39-50 API is no longer available](../../api/z39-50.md#upgrading-from-geonetwork-30-guidance)
   * [OpenSearch no longer available](../../api/opensearch.md#upgrading-from-geonetwork-30-guidance)
   * [Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH) is not migrated](../../api/oai-pmh.md#upgrading-from-geonetwork-30-guidance)
   * [GeoNetwork API support for Report Uploads not migrated](../../api/the-geonetwork-api.md#upgrading-from-geonetwork-3-guidance)
   
   The above links provided recommendations and mitigation measures where appropriate.

* For information on new features and functionality:
  
   * [Changelog 4.4.x](../../overview/change-log/latest/index.md)
   * [Changelog 4.0.x](../../overview/change-log/archive/index.md#40x)
