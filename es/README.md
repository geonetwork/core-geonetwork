## Install, configure and start Elasticsearch

### Manual installation

Download Elasticsearch (at least 7.2.1 for Geonetwork 3.8.x) from https://www.elastic.co/fr/downloads/elasticsearch
and copy it to the ES module. eg. es/elasticsearch-7.2.1
 
Start ES using:

```
./bin/elasticsearch
```


### Maven installation

Maven can take care of the installation steps:
* download
* initialize collection
* start

Use the following commands:

```
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
```

### Configure index

Change to the es directory and load the indices:

```
curl -X PUT http://localhost:9200/gn-records -H "Content-Type:application/json"  -d @config/records.json
curl -X PUT http://localhost:9200/gn-features -H "Content-Type:application/json" -d @config/features.json
curl -X PUT http://localhost:9200/gn-searchlogs -H "Content-Type:application/json"  -d @config/searchlogs.json
```
To delete your index:

```
curl -X DELETE http://localhost:9200/gn-records
curl -X DELETE http://localhost:9200/gn-features
curl -X DELETE http://localhost:9200/gn-searchlogs
```
 Check that elasticsearch is running by visiting http://localhost:9200 in a browser


### Production use

Configure ES to start on server startup.

 * Note that for debian-based servers the current deb download (7.3.2) can be installed rather than installing manually and can be configured to run as a service using the instructions here: https://www.elastic.co/guide/en/elasticsearch/reference/current/starting-elasticsearch.html

