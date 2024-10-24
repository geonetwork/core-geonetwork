# Harvesting CSW services

This harvester will connect to a remote CSW server and retrieve metadata records that match the query parameters specified.

## Adding a CSW harvester

The figure above shows the options available:

-   **Site** - Options about the remote site.
    -   *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the CSW harvester.
    -   *Service URL* - The URL of the capabilities document of the CSW server to be harvested. eg. <http://geonetwork-site.com/srv/eng/csw?service=CSW&request=GetCabilities&version=2.0.2>. This document is used to discover the location of the services to call to query and retrieve metadata.
    -   *Icon* - An icon to assign to harvested metadata. The icon will be used when showing harvested metadata records in the search results.
    -   *Use account* - Account credentials for basic HTTP authentication on the CSW server.
-   **Search criteria** - Using the Add button, you can add several search criteria. You can query only the fields recognised by the CSW protocol.
-   **Options** - Scheduling options.
-   **Options** - Specific harvesting options for this harvester.
    -   *Validate* - If checked, the metadata will be validated after retrieval. If the validation does not pass, the metadata will be skipped.
-   **Privileges** - Assign privileges to harvested metadata.
-   **Categories**
