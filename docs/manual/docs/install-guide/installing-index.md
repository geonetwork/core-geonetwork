# Installing search platform {#installing-index}

The GeoNetwork search engine is built on top of Elasticsearch. The platform is used to index records and also to analyze WFS data (See [Analyze and visualize data](../user-guide/analyzing/data.md) ), an [Elasticsearch](https://www.elastic.co/products/elasticsearch) instance must be installed next to the catalog.

## Manual installation

Download Elasticsearch 7.x (at least `7.9.2`) from <https://www.elastic.co/downloads/elasticsearch> and unzip the file.

``` shell
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.9.2.tar.gz
tar xvfz elasticsearch-7.9.2.tar.gz
```

Manually start and stop Elasticsearch using:

``` shell
elasticsearch-7.9.2/bin/elasticsearch
```

Stop Elasticsearch using

``` shell
elasticsearch-7.9.2/bin/elasticsearch stop
```

GeoNetwork will start even if Elasticsearch index is down. A warning will be displayed. Once the Elasticsearch index is up, indices are created if they do not exist.

(Optional) Then create the default index (the application will create them automatically once the index is up and running and if no indices are found):

## Customizing index

User may want to customize the index settings for example to change language configuration (see `es/README.md`). Check the configuration file in `` `$GN_DATA_DIRECTORY/config/index ```. To manually remove and recreate the index, use the following:

``` shell
cd $GN_DATA_DIRECTORY/config/index
curl -X DELETE http://localhost:9200/features
curl -X DELETE http://localhost:9200/records
curl -X DELETE http://localhost:9200/searchlogs

curl -X PUT http://localhost:9200/features -H 'Content-Type: application/json' -d @features.json
curl -X PUT http://localhost:9200/records -H 'Content-Type: application/json' -d @records.json
curl -X PUT http://localhost:9200/searchlogs -H 'Content-Type: application/json' -d @searchlogs.json
```

## Install using Maven

Running from the source code, use Maven to download.

``` shell
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
```

To stop Elasticsearch when using Maven, simply stop the process as Elasticsearch is started in foreground mode.

## Check installation

Access Elasticsearch admin page from <http://localhost:9200/>.

## Configure connection

Update Elasticsearch connection details in ```WEB-INF/config.properties``` and restart the application:

``` shell
es.protocol=http
es.port=9200
es.host=localhost
es.url=${es.protocol}://${es.host}:${es.port}
es.username=
es.password=
```

It is not needed nor recommended to open port 9200 to the outside. GeoNetwork is protecting the Elasticsearch instance exposing only the search API and taking care of user privileges.
