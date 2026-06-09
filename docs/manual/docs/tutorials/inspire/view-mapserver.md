# Setting up INSPIRE view service with GeoNetwork and Mapserver {#tuto-view-mapserver}

This tutorial shows how one can set up a combination of [MapServer](https://mapserver.org) and GeoNetwork to provide view services following [technical guidelines for view services](https://inspire.ec.europa.eu/documents/Network_Services/TechnicalGuidance_ViewServices_v3.1.pdf).

## MapServer

How to set up an INSPIRE view service in Mapserver is documented in [mapserver documentation](https://www.mapserver.org/ogc/inspire.html). In this tutorial we use the reference service metadata approach:

``` text
WEB
 METADATA
  "wms_inspire_capabilities" "url"
 END
END
```

## GeoNetwork

When deploying Geonetwork, make sure the GEMET thesauri are loaded and activate the INSPIRE editor as described in [Geonetwork documentation](../../administrator-guide/configuring-the-catalog/inspire-configuration.md).

In Admin --> Settings activate the INSPIRE extension.

![image](img/image_3.png)

For each dataset that you are going to publish create an iso19115 record using the INSPIRE template. Link each record to the view service as created in mapserver: eg ``https://{url}/cgi-bin/mapserv?map={mapfile}&request=getcapabilities&service=wms&version=1.3.0``

![image](img/image_5.png)

Make sure the metadata contains the same gmd:code (and authority) as available in WMS getcapabilities.

Create an OGC harvester that is able to extract a metadata for service (iso19119) record from the WMS. Run the harvester and note down the identifier of the created service metadata.

## Return to MapServer Mapfile

For each layer configuration add a metadata url of type text/xml. Other relevant parameters are the authority element and the dataset identifier.

``` text
LAYER
 NAME "mylayer"
 METADATA
  wms_dataurl_format "application/vnd.ogc.csw.GetRecordByIdResponse_xml"
  wms_dataurl_href "http://geonetwork/srv/api/records/f4f137aa-a2bf-4033-91ef-2cfdbe500690"
  wms_authorityurl_name "inspire" 
  wms_authorityurl_href "http://inspire.ec.europa.eu/"
  wms_identifier_authority "inspire"
  wms_identifier_value "0a636f43-016c-474a-ab28-1f3d75e9fcae"
 END
END
```

For the service definition add a link to the service metadata

``` text
WEB
 METADATA
  "wms_inspire_capabilities" "url"
  "wms_languages" "eng"               
  "wms_inspire_metadataurl_href" "http://geonetwork/srv/api/records/d461302e-5ec8-415d-9a6d-05de37184b03"
  "wms_inspire_metadataurl_format" "application/vnd.ogc.csw.GetRecordByIdResponse_xml"
  "wms_keywordlist_ISO_items" "infoMapAccessService"
 END 
END
```

## Validate the implementation

If you are running the above setup online, you can use the [pilot JRC INSPIRE validator](https://inspire-geoportal.ec.europa.eu/validator2/). If the above setup is running locally, you can use [Esdin Test Framework](https://github.com/Geonovum/etf-test-projects-inspire) to validate the INSPIRE setup.

![image](img/image_6.png)

Running the test frequently during development helps to identify issues in an early stage.

## Known issues

There is a known issue in the capabilities to metadata linkage. The JRC validator requires a gmd:RS_Identifier inside gmd:code having the authority and dataset identifier modeled separately. However the technical guidelines suggest a gmd:MD_Identifier inside gmd:code, the authority can then be included as a prefix, eg ``<gmd:MD_Identifier>{authority}#{uuid}<gmd:MD_Identifier>``
