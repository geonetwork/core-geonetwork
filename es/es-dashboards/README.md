# Install, configure and start Kibana

## Installation options

### Docker compose installation (Recommended)

1. Use docker compose with the provided [docker-compose.yml](es/docker-compose.yml):

   ```
   cd es
   docker-compose up
   ```

3. Check that it is running using your browser:
   
   * Elasticsearch: http://localhost:9200
   * Kibana: http://localhost:5601/api/status

## Maven installation

1. Maven can take care of the installation steps:

   * download
   * initialize collection
   * start

2. Use Maven to download:

   ```
   cd es/es-dashboards
   mvn install -Pkb-download
   ```

3. Set Kibana base path in config/kibana.yml:

   ```
   server.basePath: "/geonetwork/dashboards"
   server.rewriteBasePath: false
   ```

4. Run locally:

   ```
   mvn exec:exec -Dkb-start
   ```

## Manual installation

1. Download Kibana 8.14.3 from https://www.elastic.co/downloads/kibana

2. Set Kibana base path in config/kibana.yml:

   ```
   server.basePath: "/geonetwork/dashboards"
   server.rewriteBasePath: false
   ```

3. Adapt if needed ```elasticsearch.url``` and ```server.host```.

4. Start Kibana manually:

   ```
   cd kibana/bin
   ./kibana
   ```

## Import Configuration

1. The Kibana status details should be available at:

   ```
   http://localhost:5601/api/status
   ```

2. And should be visible within the geonetwork interface at:
 
   ```
   http://localhost:8080/geonetwork/dashboards
   ```


## Troubleshoot

If it does not start properly, check Kibana log files (eg. it may fail if Elasticsearch version
is not compatible with Kibana version).

Visit Kibana in a browser using one of the above links and go to 'Saved Objects'. Import export.ndjson from https://github.com/geonetwork/core-geonetwork/blob/main/es/es-dashboards/data/export.ndjson

### Production Use

Kibana can be installed from the debian files, and Kibana 8.14.3 is confirmed as working with Geonetwork 4.4.x.

Set Kibana to start when the server starts up, using the instructions at https://www.elastic.co/guide/en/kibana/current/start-stop.html



