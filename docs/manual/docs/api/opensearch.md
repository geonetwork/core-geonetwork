# OpenSearch

!!! warning

    Unavailable since version 4.0.0.

    There is no known sponsor or interested party for implementing OpenSearch.

The OpenSerach API provides a serivce description advertised in the HTML.

Browsers detect the availability of opensearch by checking the index page at the root of the (sub)domain. Setup required defining a rewrite rule forwarding requests to the geonetwork application.

Reference:

* [OpenSearch](https://www.ogc.org/standard/opensearch/) (Open Geospatial Consortium)

## Upgrading from GeoNetwork 3.0 Guidance

OpenSearch API is no longer available.

* Recommend migrating to [GeoNetwork OpenAPI](the-geonetwork-api.md) if html discoverability is of primary importance.

  This provides a self-describing service, and automation tools for developer access in different programming languages.
  However the result is specific to the GeoNetwork application, and not an industry standard for interoperability.

* Recommend migration to [OpenGIS Web Catalogue Service (CSW)](csw.md) if standards compliance is of primary importance.

!!! note OGC API - Records
    
    The OGC API - Records standard is not yet ready, but is expected to provide the best of both worlds: html discoverability, and standards compliance.
    
    Interested parties are encouraged to contribute towards this roadmap activity.
