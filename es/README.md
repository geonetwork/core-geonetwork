# Install, configure and start Elasticsearch

## Installation options

This section describes several methods for configuring Elasticsearch for development.

These configurations should not be used for a production deployment.

### Docker installation (Recommended)

1. Use docker pull to download the image (you can check version in the :file:`pom.xml` file):

   ```
   docker pull docker.elastic.co/elasticsearch/elasticsearch:8.14.0
   ```

2. Use docker run, leaving 9200 available:

   ```
   docker run -p 9200:9200 -p 9300:9300 \ 
      -e "discovery.type=single-node" \
      -e "xpack.security.enabled=false" \
      -e "xpack.security.enrollment.enabled=false" \ 
      docker.elastic.co/elasticsearch/elasticsearch:8.14.0
   ```

3. Check that elasticsearch is running by visiting http://localhost:9200 in a browser

### Docker compose installation

1. Use docker compose with the provided [docker-compose.yml](docker-compose.yml):

   ```
   cd es
   docker-compose up
   ```

3. Check that it is running using your browser:
   
   * Elasticsearch: http://localhost:9200
   * Kibana: http://localhost:5601

### Maven installation

Maven installation ensure you always are using the ``es.version`` version specified in ``pom.xml``.

1. Maven can take care of the installation steps:

   * download
   * initialize collection
   * start

2. Use the following commands:

   ```shell script
   cd es
   mvn install -Pes-download
   mvn exec:exec -Des-start
   ```
3. Check that elasticsearch is running by visiting http://localhost:9200 in a browser

## Manual installation

1. Download Elasticsearch 8.14.0 from https://www.elastic.co/downloads/elasticsearch
and copy to the ES module, e.g., ``es/elasticsearch-8.14.0`

2. Disable the security

   Elasticsearch 8 has security enabled by default. To disable this configuration for development, update the file `config/elasticsearch.yml` adding at the end:

   ```
   xpack.security.enabled: false
   xpack.security.enrollment.enabled: false
   ```

3. Start ES using:

   ```shell script
   ./bin/elasticsearch
   ```

4. Check that elasticsearch is running by visiting http://localhost:9200 in a browser

# Configuration

## Index management

Optionally you can manually create index but they will be created by the catalogue when 
the Elastic instance is available and if index does not exist.

```shell script
IDX_PREFIX=gn
curl -X PUT http://localhost:9200/$IDX_PREFIX-records -H "Content-Type:application/json"  -d @../web/src/main/webapp/WEB-INF/data/config/index/records.json
curl -X PUT http://localhost:9200/$IDX_PREFIX-features -H "Content-Type:application/json" -d @../web/src/main/webapp/WEB-INF/data/config/index/features.json
curl -X PUT http://localhost:9200/$IDX_PREFIX-searchlogs -H "Content-Type:application/json"  -d @../web/src/main/webapp/WEB-INF/data/config/index/searchlogs.json
```

To delete your index:

```shell script
IDX_PREFIX=gn
curl -X DELETE http://localhost:9200/$IDX_PREFIX-records
curl -X DELETE http://localhost:9200/$IDX_PREFIX-features
curl -X DELETE http://localhost:9200/$IDX_PREFIX-searchlogs
```

## Multilingual configuration

Default index is configured with analyzer for the following languages:
* English
* French
* German
* Italian
  
To add a new language, update the index schema in `datadir/config/index/records.json` and update the containing fields starting with `lang`.

First create a full text search field for the new language in the `any` object field eg. `any.langfre` and define the proper analyzer.

Then add the new language like the others.

From the admin console > tools, Delete index and reindex.

Don't hesitate to propose a Pull Request with the new language.


# Production use

1. Configure ES to start on server startup. It is recommended to protect `gn-records` index from the Internet access.

   * Note that for debian-based servers the current deb download (8.14.0) can be installed rather than installing manually and can be configured to run as a service using the instructions here: https://www.elastic.co/guide/en/elasticsearch/reference/current/starting-elasticsearch.html


# Troubleshoot

## Max number of fields

Max number of fields: As we are using dynamic fields, when having a large number of records, the system may reach the maximum number of fields in Elasticsearch. In this situation, Elasticsearch is reporting: 

```
java.lang.IllegalArgumentException: Limit of total fields [1000] in index [gn-records] has been exceeded
```

Increase the limit using:

```shell script
curl -X PUT localhost:9200/gn-records/_settings -H "Content-Type:application/json"  -d '
{
  "index.mapping.total_fields.limit": 6000
}'
```

To check the current number of fields in the index use:

```shell script
curl -s -XGET localhost:9200/gn-records/_mapping?pretty | grep type | wc -l
```

See https://www.elastic.co/guide/en/elasticsearch/reference/master/mapping.html#mapping

## Field expansion limit

The client application will report "Query returned an error. Check the console for details" and the error contains the following:

```
field expansion for [*] matches too many fields, limit: 1024
```

An option is to restrict `queryBase` to limit the number of field to query on. `any:(${any}) resourceTitleObject.default:(${any})^2` is a good default. Using `${any}` will probably trigger the error if the number of records is high.

The other option is to increase `indices.query.bool.max_clause_count`.


## Disk space threshold

The server application will refuse to write new content unless there is enough free space available (by default 1/4 of your hard drive).

To turn off this check:

```
 curl -XPUT http://localhost:9200/_cluster/settings -H 'Content-Type: application/json' -d '{ "transient" : { "cluster.routing.allocation.disk.threshold_enabled" : false } }' 
```

## Blocked by index read-only / allow delete

To recover:

```
curl -XPUT -H "Content-Type: application/json" http://localhost:9200/_all/_settings -d '{"index.blocks.read_only_allow_delete": null}'
```
