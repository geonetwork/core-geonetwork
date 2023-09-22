# GeoPortal REST Harvesting {#geoportal_rest_harvester}

This harvester will connect to a remote GeoPortal version 9.3.x or 10.x server and retrieve metadata records that match the query parameters specified using the GeoPortal REST API.

## Adding a GeoPortal REST harvester

The figure above shows the options available:

-   **Site** - Options about the remote site.
    -   *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the GeoPortal REST harvester.
    -   *Base URL* - The base URL of the GeoPortal server to be harvested. eg. <http://yourhost.com/geoportal>. The harvester will add the additional path required to access the REST services on the GeoPortal server.
    -   *Icon* - An icon to assign to harvested metadata. The icon will be used when showing harvested metadata records in the search results.
-   **Search criteria** - Using the Add button, you can add several search criteria. You can query any field on the GeoPortal server using the Lucene query syntax described at <http://webhelp.esri.com/geoportal_extension/9.3.1/index.htm#srch_lucene.htm>.
-   **Options** - Scheduling options.
-   **Harvested Content** - Options that are applied to harvested content.
    -   *Apply this XSLT to harvested records* - Choose an XSLT here that will convert harvested records to a different format. See notes section below for typical usage.
    -   *Validate* - If checked, the metadata will be validated after retrieval. If the validation does not pass, the metadata will be skipped.
-   **Privileges** - Assign privileges to harvested metadata.
-   **Categories**

!!! Notes

    -   this harvester uses two REST services from the GeoPortal API:
        -   `rest/find/document` with searchText parameter to return an RSS listing of metadata records that meet the search criteria (maximum 100000)
        -   `rest/document` with id parameter from each result returned in the RSS listing
    -   this harvester has been tested with GeoPortal 9.3.x and 10.x. It can be used in preference to the CSW harvester if there are issues with the handling of the OGC standards etc.
    -   typically ISO19115 metadata produced by the Geoportal software will not have a 'gmd' prefix for the namespace `http://www.isotc211.org/2005/gmd`. GeoNetwork XSLTs will not have any trouble understanding this metadata but will not be able to map titles and codelists in the viewer/editor. To fix this problem, please select the ``Add-gmd-prefix`` XSLT for the *Apply this XSLT to harvested records* in the **Harvested Content** set of options described earlier
