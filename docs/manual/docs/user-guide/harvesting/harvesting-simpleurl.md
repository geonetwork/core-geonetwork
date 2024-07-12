# Simple URL harvesting (opendata) {#harvesting-simpleurl-services}

This harvester connects to a remote server via a simple URL to retrieve metadata records. This allows harvesting opendata catalogs such as opendatasoft, ESRI, DKAN and more.

## Adding a simple URL harvester

-   **Site** - Options about the remote site.

    -   *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the harvester.
    -   *Service URL* - The URL of the server to be harvested. This can include pagination params like `?start=0&rows=20`
    -   *loopElement* - Propery/element containing a list of the record entries. (Indicated as an absolute path from the document root.) eg. `/datasets`
    -   *numberOfRecordPath* : Property indicating the total count of record entries. (Indicated as an absolute path from the document root.) eg. `/nhits`
    -   *recordIdPath* : Property containing the record id. eg. `datasetid`
    -   *pageFromParam* : Property indicating the first record item on the current "page" eg. `start`
    -   *pageSizeParam* : Property indicating the number of records containned in the current "page" eg. `rows`
    -   *toISOConversion* : Name of the conversion schema to use, which must be available as XSL on the GN instance. eg. `OPENDATASOFT-to-ISO19115-3-2018`

    !!! note

        GN looks for schemas by name in <https://github.com/geonetwork/core-geonetwork/tree/4.0.x/web/src/main/webapp/xsl/conversion/import>. These schemas might internally include schemas from other locations like <https://github.com/geonetwork/core-geonetwork/tree/4.0.x/schemas/iso19115-3.2018/src/main/plugin/iso19115-3.2018/convert>. To indicate the `fromJsonOpenDataSoft` schema for example, from the latter location directly in the admin UI the following syntax can be used: `schema:iso19115-3.2018:convert/fromJsonOpenDataSoft`.


    **Sample configuration for opendatasoft**

    -   *loopElement* - `/datasets`
    -   *numberOfRecordPath* : `/nhits`
    -   *recordIdPath* : `datasetid`
    -   *pageFromParam* : `start`
    -   *pageSizeParam* : `rows`
    -   *toISOConversion* : `OPENDATASOFT-to-ISO19115-3-2018`

    **Sample configuration for ESRI**

    -   *loopElement* - `/dataset`
    -   *numberOfRecordPath* : `/result/count`
    -   *recordIdPath* : `landingPage`
    -   *pageFromParam* : `start`
    -   *pageSizeParam* : `rows`
    -   *toISOConversion* : `ESRIDCAT-to-ISO19115-3-2018`

    **Sample configuration for DKAN**

    -   *loopElement* - `/result/0`
    -   *numberOfRecordPath* : `/result/count`
    -   *recordIdPath* : `id`
    -   *pageFromParam* : `start`
    -   *pageSizeParam* : `rows`
    -   *toISOConversion* : `DKAN-to-ISO19115-3-2018`

-   **Privileges** - Assign privileges to harvested metadata.
