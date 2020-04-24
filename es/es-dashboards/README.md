## Install, configure and start Kibana

### Manual installation

Download Kibana from https://www.elastic.co/fr/downloads/kibana. For Geonetwork 3.8.x download at least version 7.2.1

Set Kibana base path and index name in config/kibana.yml:

```
server.basePath: "/geonetwork/dashboards"
server.rewriteBasePath: false
kibana.index: ".dashboards"

```

Adapt if needed ```elasticsearch.url``` and ```server.host```.



Start Kibana manually:

```
cd kibana/bin
./kibana
```

### Maven installation

Maven can take care of the installation steps:
* download
* initialize collection
* start

Use the following commands:

```
cd es/es-dashboard
mvn install -Pkb-download
mvn exec:exec -Dkb-start
```

### Import Configuration

Kibana should be running from:

```
http://localhost:5601

```
 and should be visible within the geonetwork interface at:
 
```
http://localhost:8080/geonetwork/dashboards

```

If it does not start properly, check Kibana log files (eg. it may fail if Elasticsearch version
is not compatible with Kibana version).

Visit Kibana in a browser using one of the above links and go to 'Saved Objects'. Import export.json from https://github.com/geonetwork/core-geonetwork/blob/master/es/es-dashboards/data/export.json

### Production Use

Kibana can be installed from the debian files, and 7.3.2 is confirmed as working with Geonetwork 3.8.x.

Set Kibana to start when the server starts up, using the instructions at https://www.elastic.co/guide/en/kibana/current/start-stop.html



