# Installing remote index {#installing-index}

If users want to analyze WFS data (See [Analyze and visualize data](../user-guide/analyzing/data.md) ), an [Elasticsearch](https://www.elastic.co/products/elasticsearch) instance can be installed next to the catalog.

## Manual installation

Download Elasticsearch from <https://www.elastic.co/downloads/elasticsearch> and unzip the file.

``` shell
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.4.2.tar.gz
tar xvfz elasticsearch-7.4.2.tar.gz
```

Manually start and stop Elasticsearch using:

``` shell
elasticsearch-7.4.2/bin/elasticsearch
```

Then create the default index:

``` shell
cd es
curl -X PUT http://localhost:9200/features -H "Content-Type: application/json" -d @config/features.json
curl -X PUT http://localhost:9200/records -H "Content-Type: application/json" -d @config/records.json
curl -X PUT http://localhost:9200/searchlogs -H "Content-Type: application/json" -d @config/searchlogs.json
```

Stop Elasticsearch using

``` shell
elasticsearch-7.4.2/bin/elasticsearch stop
```

## Install using Maven

Running from the source code, use Maven to download.

``` shell
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
curl -X PUT http://localhost:9200/features -H "Content-Type: application/json" -d @config/features.json
curl -X PUT http://localhost:9200/records -H "Content-Type: application/json" -d @config/records.json
curl -X PUT http://localhost:9200/searchlogs -H "Content-Type: application/json" -d @config/searchlogs.json
```

To stop Elasticsearch when using Maven, simply stop the process as Elasticsearch is started in foreground mode.

## Check installation

Access Elasticsearch admin page from <http://localhost:9200/>.

## Configure connection

Update Elasticsearch connection details in `WEB-INF/config.properties` and restart the application:

``` shell
es.protocol=http
es.port=9200
es.host=localhost
es.url=${es.protocol}://${es.host}:${es.port}
es.username=
es.password=
```

It is not needed nor recommended to open port 9200 to the outside. GeoNetwork is protecting the Elasticsearch instance exposing only the search API and taking care of user privileges.
