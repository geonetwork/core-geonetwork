# GeoNetwork 2.0 Harvester {#gn2_harvester}

This harvester is required for working with GeoNetwork 2.0. GeoNetwork 2.1 introduced a new powerful harvesting engine which is not compatible with the previous GeoNetwork version 2.0 based catalogues. 

This harvesting type is deprecated as GeoNetwork 2.0 has reached end-of-life.


| Harvester                | Harvet from                          |
| ------------------------ | ------------------------------------ |
| GeoNetwork 2.0 Harvester | GeoNetwork 2.0 Catalogue             |
| GeoNetwork 3.x Harvester | GeoNetwork 2.1 and greater           |
| GeoNetwork 3.x Harvester | GeoNetwork 3.x series                |
| GeoNetwork 4.x Harvester | GeoNetwork 4.x series                |

## Adding a GeoNetwork 2.0 Harvester

Configuration options:

- **Identification** - Options describing the remote site.
    - *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the harvester.
    - *Group* - Group that owns the harvested metadata.
    - *User* - User that owns the harvested metadata.
- **Schedule** - Schedule configuration to execute the harvester.
- **Configure connection to GeoNetwork**:
    - *Catalog URL* - The URL of the GeoNetwork server from which metadata will be harvested.
    - *Search filter* - This allows you to select metadata records for harvest based on certain criteria:
        - *Full text*
        - *Title*
        - *Abstract*
        - *Keyword*
        - *Site id* - Identifier of the source to filter the metadata to harvest.

- **Configure response processing**
    - *Remote authentication*
    - *Validate records before import*

- **Privileges** - Assign privileges to harvested metadata.
