# Setup ElasticSearch {#statistics_es}

This section describes how to setup ElasticSearch to be used in GeoNetwork to store the search/content statistics. Note that Geonetwork must have been built with the `es` profile for ElasticSearch to be used. See <https://github.com/geonetwork/core-geonetwork/tree/master/software_development> for details.

## Installation

ElasticSearch can be installed manually, or for some operating systems packages are available.

!!! note

    If installed manually, ElasticSearch must be configured as a service to ensure it starts automatically when the server is started. This is beyond the scope of this guide.


To install manually:

-   Download ElasticSearch from <http://www.elastic.co/downloads/past-releases/>. For Geonetwork 3.8.x version 7.2.x is recommended.

-   Unzip the file and copy it, for example, to ``/opt/elasticsearch``

-   Execute Elastic Search:

    ``` shell
    $ cd /opt/elasticsearch/bin
    $ ./elasticsearch &
    ```

-   Verify in a browser that ElasticSearch is running: <http://localhost:9200/>

## Load indexes

``` shell
$ cd /tmp
$ curl -O https://raw.githubusercontent.com/geonetwork/core-geonetwork/3.4.x/es/config/features.json
$ curl -X PUT http://localhost:9200/gn-features -d @features.json
$ curl -O https://raw.githubusercontent.com/geonetwork/core-geonetwork/3.4.x/es/config/records.json
$ curl -X PUT http://localhost:9200/gn-records -d @records.json
$ curl -O https://raw.githubusercontent.com/geonetwork/core-geonetwork/3.4.x/es/config/searchlogs.json
$ curl -X PUT http://localhost:9200/gn-searchlogs -d @searchlogs.json
```
