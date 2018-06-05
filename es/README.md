## Install, configure and start Elasticsearch

### Manual installation

Download Elasticsearch from https://www.elastic.co/fr/downloads/elasticsearch
and copy it to the ES module. eg. es/elasticsearch-5.0.0

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
```


Optionnaly you can manually create index but they will be created by the catalogue when 
the Elastic instance is available and if index does not exist.
```
curl -H "Content-Type: application/json" -X PUT http://localhost:9200/records -d @../web/src/main/webapp/WEB-INF/data/config/index/records.json
curl -H "Content-Type: application/json" -X PUT http://localhost:9200/features -d @../web/src/main/webapp/WEB-INF/data/config/index/features.json
curl -H "Content-Type: application/json" -X PUT http://localhost:9200/searchlogs -d @../web/src/main/webapp/WEB-INF/data/config/index/searchlogs.json
```

To delete your index:

```
curl -X DELETE http://localhost:9200/features
curl -X DELETE http://localhost:9200/records
curl -X DELETE http://localhost:9200/searchlogs
```



### Production use

Configure ES to start on server startup.

