# Setting up an Atom/OpenSearch based INSPIRE download service {#tuto-download-atom}

This tutorial shows how one can set up GeoNetwork to provide download services following [technical guidelines for download services](https://inspire.ec.europa.eu/documents/technical-guidance-implementation-inspire-download-services).

Note that the technical guidelines allow both Atom/OpenSearch as well as [WFS](https://www.opengeospatial.org/standards/wfs). This tutorial describes how to set up a download service using Atom/Opensearch.

The basics of Atom/OpenSearch is that for each dataset a number of file-downloads in various flavours (language/projection/format) is available. These flavours are advertised in a "Dataset Atom Feed" document. A set of "Dataset Atom Feed" documents is grouped into a "Service Atom Feed" document. For each "Service Atom Feed" document an OpenSearchDescription document is available. A website can refer to this OpenSearchDescription document, which enables searching through the download service using the browser search bar (OpenSearch).

See:

-   [Linking data using ATOM feeds](../../user-guide/associating-resources/linking-online-resources.md#linking-data-using-atom-feed)
-   [OpenSearch and INSPIRE ATOM](../../api/opensearch.md)

## External mode implementation

GeoNetwork implements OpenSearch on a set of Dataset- and Service Atom feeds. An OpenSearch Description document is generated for each Service Atom Feed.

GeoNetwork will soon support an internal- and external mode.

-   in internal mode GeoNetwork will generate Atom files dynamically from metadata content.
-   in external mode GeoNetwork will ingest Atom files that are linked to metadata records.

Currently only the external mode is fully supported. Atom files can be generated in any text or xml editor following the technical guidelines. To optimise the Atom support GeoNetwork uses a convention, which is currently not mandated by the technical guidelines. The convention is that any gmd:MD_Distribution having a link to an Atom file requires the protocol field to contain a defined value to indicate the distribution as being an Atom document. Default value for this protocol is INSPIRE-ATOM, but you can change it using Admin --> Settings. The benefit of this approach is that GeoNetwork doesn't need to open any file-link to determine if it is an Atom File. We've seen various member states mandate this convention in localised INSPIRE Technical Guidelines.

``` xml
<gmd:MD_DigitalTransferOptions>
 <gmd:onLine>
  <gmd:CI_OnlineResource>
   <gmd:linkage>
    <gmd:URL>http://www.broinspireservices.nl/atom/awp.atom</gmd:URL>
   </gmd:linkage>
   <gmd:protocol>
    <gco:CharacterString>INSPIRE-ATOM</gco:CharacterString>
   </gmd:protocol>
   <gmd:name>
    <gco:CharacterString>gdn.Aardwarmtepotentie</gco:CharacterString>
   </gmd:name>
  </gmd:CI_OnlineResource>
 </gmd:onLine>
</gmd:MD_DigitalTransferOptions>
```

When deploying Geonetwork, make sure the GEMET thesauri are loaded and activate the INSPIRE editor as described in [inspire configuration](../../administrator-guide/configuring-the-catalog/inspire-configuration.md). In this file "schemas/iso19139/src/main/plugin/iso19139/loc/eng/labels.xml " (and the same file in other languages in use in the catalogue) in the gmd:protocol helper (around line 2000) add the "INSPIRE-ATOM" protocol which is used to indicate links that link to an Atom file (only links having that protocol value will be ingested).

In Admin --> Settings activate the INSPIRE extension and activate Atom harvesting (at certain interval). The settings page facilitates to set a protocol which is used in metadata to indicate an Atom link.

![image](img/image_4.png)

For each dataset that you are going to publish create an iso19115 record using the INSPIRE template. Link each record to the related atom file (or upload an atom file)

![image](img/image_7.png)

Make sure the metadata contains the same gmd:code as used in the Atom file.

Now create a service metadata using the INSPIRE template and link it to the service Atom and the individual datasets (using srv:operatesOn).

Before you validate the implementation, run the Atom harvester in admin --> settings manually. A panel will display the number of processed Atom feeds and any errors that may have occurred while processing (more details in log file).

## Validate the implementation

If you are running the above setup online, you can use the [INSPIRE validator](https://inspire.ec.europa.eu/validator/). If the above setup is running locally, you can use [Esdin Test Framework](https://github.com/Geonovum/etf-test-projects-inspire) to validate the INSPIRE setup.

![image](img/image_6.png)

Running the test frequently during development helps to identify issues in an early stage.
