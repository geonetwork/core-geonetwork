# Advanced topics {#extra}

## GeoNetwork API

In recent releases most of GeoNetworks functionality is exposed via a centralised API based on OpenApi. An optimal approach to get started with the API is via the Interactive documentation page provided by OpenAPI. This page is accessibly via <http://localhost:8080/geonetwork/doc/api>.

![](img/apidoc.png){width="400px"}

The page describes each of the methods and its properties, but also offers the capability to try out any operation via a web form.

## Kibana Dashboard

Kibana is an optional component which can be installed with Elastic Search. Kibana offers a dynamic way to create visualisations of the contentn of the Elastc Search index.

The GeoNetwork community has prepared some sample visualisations. Load the samples from <https://github.com/geonetwork/core-geonetwork/blob/4.0.x/es/es-dashboards/data/export.ndjson> via the 'Saved objects --> Import' option in Kibana.

If installed, you can access kibana from the GeoNetwork admin interface. To access the Kibana interface you need to be logged in as administrator.

Read more about the usage of kibana at <https://www.elastic.co/guide/en/kibana/current/introduction.html>

## Search engines

Search engines provide a good mechanism for wide data discovery for public portals. The process of having catalogue content ingested by search engine crawlers requires some attention. The search engine expects to find robots.txt, at the root of the domain. Robots.txt contains a reference to the sitemap /srv/api/sitemap. Alternatively you can register the sitemap manually at individual search engines.

GeoNetwork includes on any html representation of a metadata record a representation of that record in schema.org encoded as json-ld. This enables the search engine to extract the information in a structured way.
