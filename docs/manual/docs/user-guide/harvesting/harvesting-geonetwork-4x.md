# GeoNetwork 4.x Harvester {#gn4_harvester}

GeoNetwork 4.x changed the search engine to Elasticsearch, that is not compatible with previous versions. To harvest 
a catalogue based on GeoNetwork 4.x requires this harvesting type.

| Harvester                | Harvet from                          |
| ------------------------ | ------------------------------------ |
| GeoNetwork 2.0 Harvester | GeoNetwork 2.0 Catalogue             |
| GeoNetwork 3.x Harvester | GeoNetwork 2.1 and greater           |
| GeoNetwork 3.x Harvester | GeoNetwork 3.x series                |
| GeoNetwork 4.x Harvester | GeoNetwork 4.x series                |
## Adding a GeoNetwork 4.x Harvester

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
        - *Catalog* - Allows to select a source to filter the metadata to harvest.

- **Configure response processing**
    - *Action on UUID collision* - Allows to configure the action when a harvester finds the same uuid on a record collected by another method (another harvester, importer, dashboard editor,...).
        - skipped (default)
        - overriden
        - generate a new UUID
    - *Remote authentication*
    - *Use full MEF format*
    - *Use change date for comparison*
    - *Set category if it exists locally*
    - *Category for harvested records*
    - *XSL filter name to apply*
    - *Validate records before import*

- **Privileges** - Assign privileges to harvested metadata.

