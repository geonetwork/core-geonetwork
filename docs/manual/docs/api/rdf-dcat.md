# RDF DCAT end point {#rdf-dcat}

!!! warning

    Unavailable since version 4.0.0.
    
    There is no known sponsor or interested party for implementing RDF DCAT.
    Interested parties may contact the project team for guidance and to express their intent.

The RDF DCAT end point provides a way of getting information about the catalog, the datasets and services, and links to distributed resources in a machine-readable format. The formats of the output are based on DCAT, an RDF vocabulary that is designed to facilitate interoperability between web-based data catalogs.

Reference:

* [Data Catalog Vocabulary (DCAT)](https://www.w3.org/TR/vocab-dcat-3/)

## Upgrading from GeoNetwork 3.0 Guidance

RDF DCAT API is no longer available.

1. We recommend migrating to use of [Catalog Service for the Web (CSW)](csw.md) API to query and explore data.

2. When downloading using `GetRecord` make use of the `application/rdf+xml; charset=UTF-8` output format.
   
   This will allow retrieving records in the same document format as previously provided by RDF DCAT api.
