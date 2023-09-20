# RDF DCAT end point {#rdf-dcat}

!!! warning

    Not yet available in version 4.


The RDF DCAT end point provides a way of getting information about the catalog, the datasets and services, and links to distributed resources in a machine-readable format. The formats of the output are based on DCAT, an RDF vocabulary that is designed to facilitate interoperability between web-based data catalogs.

## URLS

The following URLs are available (substitute your GeoNetwork URL):

-   <http://localhost:8080/geonetwork/srv/eng/rdf.metadata.get?uuid=> : returns an RDF record for the given UUID
-   <http://localhost:8080/geonetwork/srv/eng/rdf.search>?: returns a dcat:Catalog record. By default this will describe all the records in the catalog, but query filters are available (see below)

## Query parameters

-   `_cat`: Metadata Category
