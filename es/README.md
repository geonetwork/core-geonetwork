## Install, configure and start Elasticsearch

### Manual installation

Download Elasticsearch from https://www.elastic.co/fr/downloads/elasticsearch
and copy it to the ES module. eg. es/elasticsearch-5.0.0

or run "mvn install -Pes-download"

Start ES.

Configure index
```
curl -X PUT http://localhost:9200/features -d @config/features.json
```

### Maven installation

Maven could take care of the installation steps:
* download
* initialize collection
* start

Use the following commands:

```
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
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



### Production use

Configure ES to start on server startup.

