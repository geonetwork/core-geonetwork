# Z3950 Harvesting {#z3950_harvester}

Z3950 is a remote search and harvesting protocol that is commonly used to permit search and harvest of metadata. Although the protocol is often used for library catalogs, significant geospatial metadata catalogs can also be searched using Z3950 (eg. the metadata collections of the Australian Government agencies that participate in the Australian Spatial Data Directory - ASDD). This harvester allows the user to specify a Z3950 query and retrieve metadata records from one or more Z3950 servers.

## Adding a Z3950 Harvester

The available options are:

-   **Site**
    -   *Name* - A short description of this Z3950 harvester. It will be shown in the harvesting main page using this name.
    -   *Z3950 Server(s)* - These are the Z3950 servers that will be searched. You can select one or more of these servers.
    -   *Z3950 Query* - Specify the Z3950 query to use when searching the selected Z3950 servers. At present this field is known to support the Prefix Query Format (also known as Prefix Query Notation) which is described at this URL: <http://www.indexdata.com/yaz/doc/tools.html#PQF>. See below for more information and some simple examples.
    -   *Icon* - An icon to assign to harvested metadata. The icon will be used when showing search results.
-   **Options** - Scheduling options.
-   **Harvested Content**
    -   *Apply this XSLT to harvested records* - Choose an XSLT here that will convert harvested records to a different format.
    -   *Validate* - If checked, records that do not/cannot be validated will be rejected.
-   **Privileges**
-   **Categories**

!!! note

    this harvester automatically creates a new Category named after each of the Z3950 servers that return records. Records that are returned by a server are assigned to the category named after that server.


## More about PQF Z3950 Queries

PQF is a rather arcane query language. It is based around the idea of attributes and attribute sets. The most common attribute set used for geospatial metadata in Z3950 servers is the GEO attribute set (which is an extension of the BIB-1 and GILS attribute sets - see <http://www.fgdc.gov/standards/projects/GeoProfile>). So all PQF queries to geospatial metadata Z3950 servers should start off with @attrset geo.

The most useful attribute types in the GEO attribute set are as follows:

| @attr number | Meaning    | Description                                      |
|---------------|------------|--------------------------------------------------|
| 1             | Use        | What field to search                             |
| 2             | Relation   | How to compare the term specified                |
| 4             | Structure  | What type is the term? eg. date, numeric, phrase |
| 5             | Truncation | How to truncate eg. right                        |

In GeoNetwork the numeric values that can be specified for `@attr 1` map to the lucene index field names as follows:

| @attr 1=            | Lucene index field            | ISO19139 element                                                                                            |
|----------------------|-------------------------------|-------------------------------------------------------------------------------------------------------------|
| 1016                 | any                           | All text from all metadata elements                                                                         |
| 4                    | title, altTitle               | gmd:identificationInfo//gmd:citation//gmd:title/gco:CharacterString                                         |
| 62                   | abstract                      | gmd:identificationInfo//gmd:abstract/gco:CharacterString                                                    |
| 1012                 | _changeDate                  | Not a metadata element (maintained by GeoNetwork)                                                           |
| 30                   | createDate                    | gmd:MD_Metadata/gmd:dateStamp/gco:Date                                                                      |
| 31                   | publicationDate               | gmd:identificationInfo//gmd:citation//gmd:date/gmd:<CI_DateCode/@codeListValue>='publication'             |
| 2072                 | tempExtentBegin               | gmd:identificationInfo//gmd:extent//gmd:temporalElement//gml:begin(Position)                                |
| 2073                 | tempExtentEnd                 | gmd:identificationInfo//gmd:extent//gmd:temporalElement//gml:end(Position)                                  |
| 2012                 | fileId                        | gmd:MD_Metadata/gmd:fileIdentifier/*                                                                       |
| 12                   | identifier                    | gmd:identificationInfo//gmd:citation//gmd:identifier//gmd:code/*                                           |
| 21,29,2002,3121,3122 | keyword                       | gmd:identificationInfo//gmd:keyword/*                                                                      |
| 2060                 | northBL,eastBL,southBL,westBL | gmd:identificationInfo//gmd:extent//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude*/gco:Decimal (etc) |

Note that this is not a complete set of the mappings between Z3950 GEO attribute set and the GeoNetwork lucene index field names for ISO19139. Check out INSTALL_DIR/web/geonetwork/xml/search/z3950Server.xsl and INSTALL_DIR/web/geonetwork/xml/schemas/iso19139/index-fields.xsl for more details and annexe A of the GEO attribute set for Z3950 at <http://www.fgdc.gov/standards/projects/GeoProfile/annex_a.html> for more details.

Common values for the relation attribute (`@attr=2`):

| @attr 2= | Description              |
|-----------|--------------------------|
| 1         | Less than                |
| 2         | Less than or equal to    |
| 3         | Equals                   |
| 4         | Greater than or equal to |
| 5         | Greater than             |
| 6         | Not equal to             |
| 7         | Overlaps                 |
| 8         | Fully enclosed within    |
| 9         | Encloses                 |
| 10        | Fully outside of         |

So a simple query to get all metadata records that have the word 'the' in any field would be:

`@attrset geo @attr 1=1016 the`

-   `@attr 1=1016` means that we are doing a search on any field in the metadata record

A more sophisticated search on a bounding box might be formulated as:

`@attrset geo @attr 1=2060 @attr 4=201 @attr 2=7 "-36.8262 142.6465 -44.3848 151.2598`

-   `@attr 1=2060` means that we are doing a bounding box search
-   `@attr 4=201` means that the query contains coordinate strings
-   `@attr 2=7` means that we are searching for records whose bounding box overlaps the query box specified at the end of the query

!!! Notes

    -   Z3950 servers must be configured for GeoNetwork in `INSTALL_DIR/web/geonetwork/WEB-INF/classes/JZKitConfig.xml.tem`
    -   every time the harvester runs, it will remove previously harvested records and create new ones.
