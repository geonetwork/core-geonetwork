# Q Search {#q-search}

!!! warning

    Unavailable since version 4.0.0.

The Q Search endpoint was built using the GeoNetwork 3.0 Lucene search engine and is no longer available.

## Upgrading from GeoNetwork 3.0 Guidance

The Q Search endpoint is replaced by the Elasticsearch ``/srv/api/search/records/_search`` endpoint.

GeoNetwork 3.0 scripts will need to be migrated to the Elasticsearch API, using POST requests in the Elasticsearch syntax.
