# GeoNetwork 2.1-3.x Harvester {#gn3_harvester}

GeoNetwork 2.1 introduced a new powerful harvesting engine which is not compatible with GeoNetwork version 2.0 
based catalogues. To harvest GeoNetwork servers based on versions 2.1 or 3.x requires this harvesting type.

| Harvester                | Harvet from                          |
| ------------------------ | ------------------------------------ |
| GeoNetwork 2.0 Harvester | GeoNetwork 2.0 Catalogue             |
| GeoNetwork 3.x Harvester | GeoNetwork 2.1 and greater           |
| GeoNetwork 3.x Harvester | GeoNetwork 3.x series                |
| GeoNetwork 4.x Harvester | GeoNetwork 4.x series                |
## Adding a GeoNetwork 2.1-3.x Harvester

Configuration options:

- **Identification** - Options describing the remote site.
    - *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the harvester.
    - *Group* - Group that owns the harvested metadata.
    - *User* - User that owns the harvested metadata.
- **Schedule** - Schedule configuration to execute the harvester.
- **Configure connection to GeoNetwork**:
    - *Catalog URL* - The URL of the GeoNetwork server from which metadata will be harvested.
    - *Node name* - GeoNetwork node name to harvest, by default `srv`.
    - *Search filter* - This allows you to select metadata records for harvest based on certain criteria:
        - *Full text*
        - *Title*
        - *Abstract* 
        - *Keyword*
        - *Custom criteria* - Allows to define whatever criteria are supported by the remote node and not available in the predefined filters (eg. `similarity` set to `1` for non fuzzy search). You may specify multiple criteria separated by `;` (eg. `_schema;siteId` with values `iso19139;7fc45be3-9aba-4198-920c-b8737112d522`).
        - *Catalog* - Allows to select a source to filter the metadata to harvest.

- **Configure response processing**
    - *Action on UUID collision* - Allows to configure the action when a harvester finds the same uuid on a record collected by another method (another harvester, importer, dashboard editor,...).
        - skipped (default)
        - overriden
        - generate a new UUID
    - *Remote authentication* - User credentials to retrieved non-public metadata.
    - *Use full MEF format*
    - *Use change date for comparison*
    - *Set category if it exists locally*
    - *Category for harvested records*
    - *XSL filter name to apply*
    - *Validate records before import*

- **Privileges** - Assign privileges to harvested metadata.
