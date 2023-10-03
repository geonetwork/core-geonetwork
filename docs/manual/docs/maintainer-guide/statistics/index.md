# Setting up search/content statistics {#statistics}

Since GeoNetwork 3.4 search and content statistics are stored in [ElasticSearch](https://www.elastic.co/products/elasticsearch/) using [Kibana](https://www.elastic.co/products/kibana) dashboards to visualize them in the GeoNetwork administration application.

This guide describes the configuration required to integrate ElasticSearch/Kibana in GeoNetwork to store and visualize the search and content statistics.

GeoNetwork 3.8.x supports ElasticSearch/Kibana 7.2, other versions may not work properly.

!!! note

    This guide doesn't provide a production level setup for ElasticSearch/Kibana. Please refer to the ElasticSearch/Kibana documentation to do a proper setup/configuration for a production environment.


-   [Setup ElasticSearch](setup-elasticsearch.md)
-   [Setup Kibana](setup-kibana.md)
-   [Setup GeoNetwork](setup-geonetwork.md)
