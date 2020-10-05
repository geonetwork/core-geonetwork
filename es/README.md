## Install, configure and start Elasticsearch

### Manual installation

Download Elasticsearch (at least 7.6.2 for Geonetwork 3.8.x) from https://www.elastic.co/fr/downloads/elasticsearch
and copy it to the ES module. eg. es/elasticsearch-7.6.2
 
Start ES using:

```shell script
./bin/elasticsearch
```


### Maven installation

Maven can take care of the installation steps:
* download
* initialize collection
* start

Use the following commands:

```shell script
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
```

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

 Check that elasticsearch is running by visiting http://localhost:9200 in a browser

### Multilingual configuration (beta)


* Stop Elasticsearch
* Define which analyzer to use https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html depending on the language(s) used in your catalogue
* Build the application with the default analyzer to use (or configure it in `data/index/records.json`)

```shell script
mvn clean install -Des.default.analyzer=french_heavy
```

* Install ICU plugin (if needed)

```shell script
elasticsearch-plugin install analysis-icu
```

* Start Elasticsearch
* Drop and rebuild your index


### Production use

Configure ES to start on server startup. It is recommended to protect `gn-records` index from the Internet access.

 * Note that for debian-based servers the current deb download (7.3.2) can be installed rather than installing manually and can be configured to run as a service using the instructions here: https://www.elastic.co/guide/en/elasticsearch/reference/current/starting-elasticsearch.html


## Errors

* Max number of fields: As we are using dynamic fields, when having a large number of records, the system may reach the maximum number of fields in Elasticsearch. In this situation, Elasticsearch is reporting: 

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



* Field expansion limit: The client application will report "Query returned an error. Check the console for details" and the error contains the following:

```
field expansion for [*] matches too many fields, limit: 1024
```

An option is to restrict `queryBase` to limit the number of field to query on. `any:(${any}) resourceTitleObject.default:(${any})^2` is a good default. Using `${any}` will probably trigger the error if the number of records is high.

The other option is to increase `indices.query.bool.max_clause_count`.
