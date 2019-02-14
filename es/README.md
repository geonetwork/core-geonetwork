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

Use the following commands (when building the app, check that index name are the same as in here depending on the prefix):

```
cd es
mvn install -Pes-download
mvn exec:exec -Des-start
IDX_PREFIX=ifr-sxt
#IDX_PREFIX=geo
curl -X PUT http://localhost:9200/$IDX_PREFIX-features -H "Content-Type:application/json" -d @config/features.json
curl -X PUT http://localhost:9200/$IDX_PREFIX-records -H "Content-Type:application/json"  -d @config/records.json
curl -X PUT http://localhost:9200/$IDX_PREFIX-searchlogs -H "Content-Type:application/json"  -d @config/searchlogs.json
curl -X PUT http://localhost:9200/$IDX_PREFIX-checkpoint -H "Content-Type:application/json"  -d @config/checkpoint.json
```

To delete your index:

```
IDX_PREFIX=ifr-sxt
curl -X DELETE http://localhost:9200/$IDX_PREFIX-records
curl -X DELETE http://localhost:9200/$IDX_PREFIX-searchlogs
curl -X DELETE http://localhost:9200/$IDX_PREFIX-features
curl -X DELETE http://localhost:9200/$IDX_PREFIX-checkpoint
```



### Production use

Configure ES to start on server startup.

