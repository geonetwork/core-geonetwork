# Install, configure and start Kibana

## Manual installation

Download Kibana from https://www.elastic.co/downloads/kibana. For Geonetwork 3.8.x download at least version 7.2.1

Set Kibana base path and index name in config/kibana.yml:

```
server.basePath: "/geonetwork/dashboards"
server.rewriteBasePath: false
```

Adapt if needed ```elasticsearch.url``` and ```server.host```.

Start Kibana manually:

```
cd kibana/bin
./kibana
```

## Maven installation

1. Maven can take care of the installation steps:

   * download
   * initialize collection
   * start

2. Use maven to download:

   ```
   cd es/es-dashboard
   mvn install -Pkb-download
   ```

3. Run locally:

   ```
   mvn exec:exec -Dkb-start
   ```

## Docker compose installation

1. Use docer compose with the provided [docker-compose.yml](docker-compose.yml):

   ```
   cd es
   docker-compose up
   ```

3. Check that it is running using your browser:
   
   * Elasticsearch: http://localhost:9200
   * Kabana: http://localhost:5601
   
## Import Configuration

1. Kibana should be running from:

   ```
   http://localhost:5601
   ```

2. And should be visible within the geonetwork interface at:
 
   ```
   http://localhost:8080/geonetwork/dashboards
   ```

## Troubleshoot

If it does not start properly, check Kibana log files (eg. it may fail if Elasticsearch version
is not compatible with Kibana version).

Visit Kibana in a browser using one of the above links and go to 'Saved Objects'. Import export.ndjson from https://github.com/geonetwork/core-geonetwork/blob/4.0.x/es/es-dashboards/data/export.ndjson

### Production Use

Kibana can be installed from the debian files, and 7.3.2 is confirmed as working with Geonetwork 3.8.x.

Set Kibana to start when the server starts up, using the instructions at https://www.elastic.co/guide/en/kibana/current/start-stop.html



