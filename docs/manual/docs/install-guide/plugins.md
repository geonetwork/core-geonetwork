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
* Jcloud: `gn-datastorage-jcloud`

## Datahub integration: `gn-datahub-integration`

The [Datahub application](https://geonetwork.github.io/geonetwork-ui/main/docs/apps/datahub.html) is a modern facade to GeoNetwork provided by [the GeoNetwork-UI project](https://github.com/geonetwork/geonetwork-ui).

![datahub.png](img/datahub.png)

*Using the `gn-datahub-integration` plugin allows easily deploying multiple Datahub frontends for each portal and subportal set up in your GeoNetwork installation* ([see documentation for portals here](../administrator-guide/configuring-the-catalog/portal-configuration.md)).

### Enabling the plugin

Simply unzip the [gn-plugin-datahub-integration](https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v4.4.6/gn-plugin-datahub-integration-4.4.6-0.zip) archive and copy its content in the `WEB-INF/lib` folder of your GeoNetwork installation.

### Plugin deploymeht with Docker

Similarly to the above the plugin can be added as part of a Docker build, see example `Dockerfile` for v4.4.8 below. 

```
# Use the official GeoNetwork v4.4.8 image as the base
FROM geonetwork:4.4.8

# Set arguments for plugin version for easy updates
ARG GEONETWORK_VERSION=4.4.8
ARG PLUGIN_VERSION=${GEONETWORK_VERSION}
ARG PLUGIN_DOWNLOAD_URL="https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v${GEONETWORK_VERSION}/gn-datahub-integration-${PLUGIN_VERSION}-0.zip/download"

# Switch to root user temporarily to install packages
USER root
# Install necessary tools: wget for downloading and unzip for extracting
# The base image already installs curl and unzip, but wget is still useful for direct downloads
# We'll re-run apt-get to be safe, but it will mostly verify existing packages.
RUN apt-get update && \
    apt-get install -y \
    wget \
    unzip && \
    rm -rf /var/lib/apt/lists/*

# Switch back to the 'jetty' user as that's how the base image operates GeoNetwork
USER jetty

# # Set the working directory to the GeoNetwork installation directory
# # This is where the geonetwork.war was unzipped in the base image.
# WORKDIR /opt/geonetwork

# Download, unzip, copy only the plugin JAR, and clean up—all in one layer
RUN wget -O /tmp/gn-plugin-datahub-integration.zip ${PLUGIN_DOWNLOAD_URL} \
    && unzip /tmp/gn-plugin-datahub-integration.zip -d /tmp/plugin \
    && cp /tmp/plugin/gn-datahub-integration-${PLUGIN_VERSION}-0/lib/*.jar /opt/geonetwork/WEB-INF/lib/ \
    && rm -rf /tmp/plugin /tmp/gn-plugin-datahub-integration.zip
```
This will add datahub to the default catalogue `ROOT/geonetwork/srv/datahub/news`. Subportals are configured as outlined below. 

### Configuration

Please refer to the [Portal configuration page](../administrator-guide/configuring-the-catalog/portal-configuration.md#configuring-a-datahub-interface-for-a-sub-portal) to set up a Datahub interface once the plugin is enabled on your GeoNetwork installation,

### Building the plugin

Use the `-Pdatahub-integration` profile when building GeoNetwork: the plugin will be packaged as a JAR file in `plugins/gn-datahub-integration/target/`.
