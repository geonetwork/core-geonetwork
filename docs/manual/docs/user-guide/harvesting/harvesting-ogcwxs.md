# Harvesting OGC Services {#ogcwxs_harvester}

An OGC service implements a GetCapabilities operation that GeoNetwork, acting as a client, can use to produce metadata for the service (ISO19119) and resources delivered by the service (ISO19115/19139). This harvester supports the following OGC services and versions:

-   Web Map Service (WMS) - versions 1.0.0, 1.1.1, 1.3.0
-   Web Feature Service (WFS) - versions 1.0.0 and 1.1.0
-   Web Coverage Service (WCS) - version 1.0.0
-   Web Processing Service (WPS) - version 0.4.0 and 1.0.0
-   Catalogue Services for the Web (CSW) - version 2.0.2
-   Sensor Observation Service (SOS) - version 1.0.0

## Adding an OGC Service Harvester

Configuration options:

-   **Site**
    -   *Name* - The name of the catalogue and will be one of the search criteria.
    -   *Type* - The type of OGC service indicates if the harvester has to query for a specific kind of service. Supported type are WMS (1.0.0, 1.1.1, 1.3.0), WFS (1.0.0 and 1.1.0), WCS (1.0.0), WPS (0.4.0 and 1.0.0), CSW (2.0.2) and SOS (1.0.0).
    -   *Service URL* - The service URL is the URL of the service to contact (without parameters like "REQUEST=GetCapabilities", "VERSION=", \...). It has to be a valid URL like <http://your.preferred.ogcservice/type_wms>.
    -   *Metadata language* - Required field that will define the language of the metadata. It should be the language used by the OGC web service administrator.
    -   *ISO topic category* - Used to populate the topic category element in the metadata. It is recommended to choose one as the topic category is mandatory for the ISO19115/19139 standard if the hierarchical level is "datasets".
    -   *Type of import* - By default, the harvester produces one service metadata record. Check boxes in this group determine the other metadata that will be produced.
        -   *Create metadata for layer elements using GetCapabilities information*: Checking this option means that the harvester will loop over datasets served by the service as described in the GetCapabilities document.
        -   *Create metadata for layer elements using MetadataURL attributes*: Checkthis option means that the harvester will generate metadata from an XML document referenced in the MetadataUrl attribute of the dataset in the GetCapabilities document. If the document referred to by this attribute is not valid (eg. unknown schema, bad XML format), the GetCapabilities document is used as per the previous option.
        -   *Create thumbnails for WMS layers*: If harvesting from an OGC WMS, then checking this options means that thumbnails will be created during harvesting.
    -   *Target schema* - The metadata schema of the dataset metadata records that will be created by this harvester.
    -   *Icon* - The default icon displayed as attribution logo for metadata created by this harvester.
-   **Options** - Scheduling Options.
-   **Privileges**
-   **Category for service** - Metadata for the harvested service is assigned to the category selected in this option (eg. "interactive resources").
-   **Category for datasets** - Metadata for the harvested datasets is assigned to the category selected in this option (eg. "datasets").

!!! Notes

    -   every time the harvester runs, it will remove previously harvested records and create new records. GeoNetwork will generate the uuid for all metadata (both service and datasets). The exception to this rule is dataset metadata created using the MetadataUrl tag is in the GetCapabilities document, in that case, the uuid of the remote XML document is used instead
    -   thumbnails can only be generated when harvesting an OGC Web Map Service (WMS). The WMS should support the WGS84 projection
    -   the chosen *Target schema* must have the support XSLTs which are used by the harvester to convert the GetCapabilities statement to metadata records from that schema. If in doubt, use iso19139.
