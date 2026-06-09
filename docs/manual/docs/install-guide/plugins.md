# Plugins

Several plugins are available to expand the functionalities of a GeoNetwork instance.

Plugins are distributed in the form of single JAR files downloaded separately from the main GeoNetwork distribution. These files are available as ZIP archives alongside the main `geonetwork.war` file:

[https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v4.4.6/](https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v4.4.6/)

!!! info "How to install a plugin?"
  
     1. Download the ZIP file of the plugin
     2. Unzip the archive in the `WEB-INF/lib` folder of your GeoNetwork installation
     3. Restart the GeoNetwork server

    See below for more information and instructions specific to each plugin.

## Data storages: `gn-datastorage-*`

Support for these data storage interfaces is available through the following plugins:

* S3: `gn-datastorage-s3`  
  [See the detailed documentation here](./customizing-data-directory.md#using-a-s3-object-storage)
* CMIS: `gn-datastorage-cmis`
* JCloud: `gn-datastorage-jcloud`  
  [See the detailed documentation here](./customizing-data-directory.md#using-a-generic-cloud-object-storage-jcloud)

## Datahub integration: `gn-datahub-integration`

The [Datahub application](https://geonetwork.github.io/geonetwork-ui/main/docs/apps/datahub.html) is a modern facade to GeoNetwork provided by [the GeoNetwork-UI project](https://github.com/geonetwork/geonetwork-ui).

![datahub.png](img/datahub.png)

*Using the `gn-datahub-integration` plugin allows easily deploying multiple Datahub frontends for each portal and subportal set up in your GeoNetwork installation* ([see documentation for portals here](../administrator-guide/configuring-the-catalog/portal-configuration.md)).

### Enabling the plugin

Simply unzip the [gn-plugin-datahub-integration](https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v4.4.6/gn-plugin-datahub-integration-4.4.6-0.zip) archive and copy its content in the `WEB-INF/lib` folder of your GeoNetwork installation.

### Configuration

Please refer to the [Portal configuration page](../administrator-guide/configuring-the-catalog/portal-configuration.md#configuring-a-datahub-interface-for-a-sub-portal) to set up a Datahub interface once the plugin is enabled on your GeoNetwork installation,

### Building the plugin

Use the `-Pdatahub-integration` profile when building GeoNetwork: the plugin will be packaged as a JAR file in `plugins/gn-datahub-integration/target/`.
