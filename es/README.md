# Install, configure and start Elasticsearch

## Installation options

This section describes several methods for configuring Elasticsearch for development.

These configurations should not be used for a production deployment.


### Docker compose installation (Recommended)

> [!CAUTION]
> Ensure you have at least 10% free disk space - see [Disk space threshold](#Disk-space-threshold) to disable this. 

1. Use docker compose with the provided [docker-compose.yml](docker-compose.yml):

   ```
   cd es
   docker-compose up
   ```

3. Check that it is running using your browser:
   
   * Elasticsearch: http://localhost:9200
   * Kibana: http://localhost:5601

### Docker installation


> [!CAUTION]
> Ensure you have at least 10% free disk space - see [Disk space threshold](#Disk-space-threshold) to disable this. 

1. Use docker pull to download the image (you can check version in the :file:`pom.xml` file):

   ```
   docker pull docker.elastic.co/elasticsearch/elasticsearch:8.14.3
   ```

2. Use docker run, leaving 9200 available:

   ```
   docker run -p 9200:9200 -p 9300:9300 \ 
      -e "discovery.type=single-node" \
      -e "xpack.security.enabled=false" \
      -e "xpack.security.enrollment.enabled=false" \ 
      docker.elastic.co/elasticsearch/elasticsearch:8.14.3
   ```

3. Check that elasticsearch is running by visiting http://localhost:9200 in a browser


### Maven installation

Maven installation ensure you always are using the ``es.version`` version specified in ``pom.xml``.

> [!CAUTION]
> Ensure you have at least 10% free disk space - see [Disk space threshold](#Disk-space-threshold) to disable this. 

1. Maven can take care of the installation steps:

   * download
   * initialize collection
   * start

2. Use the following commands to download ES:

   ```shell script
   cd es
   mvn install -Pes-download 
   ```

3. Modify the Elastic Security Settings (for development)
   
   Elasticsearch 8 has security enabled by default. To disable this configuration for development, update the file `elasticsearch-<version>/config/elasticsearch.yml` and MODIFY these EXISTING entries to `false`:

      ```
      xpack.security.enabled: false
      xpack.security.enrollment.enabled: false
      ```

4. Start ElasticSearch

   ```shell script
   mvn exec:exec -Des-start
   ```

5. Check that elasticsearch is running by visiting http://localhost:9200 in a browser



## Manual installation

1. Download Elasticsearch 8.14.3 from https://www.elastic.co/downloads/elasticsearch
and copy to the ES module, e.g., ``es/elasticsearch-8.14.3`

2. Disable the security

   Elasticsearch 8 has security enabled by default. To disable this configuration for development, update the file `elasticsearch-<version>/config/elasticsearch.yml` and MODIFY these EXISTING entries to `false`:

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

   * Note that for debian-based servers the current deb download (8.14.3) can be installed rather than installing manually and can be configured to run as a service using the instructions here: https://www.elastic.co/guide/en/elasticsearch/reference/current/starting-elasticsearch.html


# Troubleshoot

The first step is to look at the ES logs - there will often be some help regarding issues here.

Second steps in checking on the ES health is to go to these ElasticSearch URLS:

* http://localhost:9200/  This will tell you if ES is running and what version.
* http://localhost:9200/gn-records See the main GeoNetwork index
* http://localhost:9200/_cluster/health/  If your status is "red" then your ES is in trouble.
   * http://localhost:9200/_cluster/allocation/explain might give an explaination of why the cluster is "red" 
   * "yellow" is expected on a single-node cluster (and is not necessarily a problem)

The third step is to get more information from GeoNetwork:

* http://localhost:8800/geonetwork/criticalhealthcheck
* http://localhost:8800/geonetwork/warninghealthcheck



One of the most common issues is insufficient disk space available - see [Disk space threshold](#Disk-space-threshold).

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

The server application will refuse to write new content unless there is enough free space available (by default 1/4 of your hard drive).  This is often described as "disk_threshold".

To turn off this check:

```
 curl -XPUT http://localhost:9200/_cluster/settings -H 'Content-Type: application/json' -d '{ "transient" : { "cluster.routing.allocation.disk.threshold_enabled" : false } }' 
```

## Blocked by index read-only / allow delete

To recover:

```
curl -XPUT -H "Content-Type: application/json" http://localhost:9200/_all/_settings -d '{"index.blocks.read_only_allow_delete": null}'
```

## Notes

* GeoNetwork uses the ElasticSearch v8 library.  This is compatible with ES v8 and v7.17.15.
* ES v8, by default, turns on security (username/password to connect to ES).  The `xpack.security.*` options, used above, turns this off (no username/password required).  If you have an ES username/password, you can configure GeoNetwork to use this with `-Des.username=<ES username>   -Des.password=<ES password>`.  To configure the ES docker container to use a specific ES password, add `- ELASTIC_PASSWORD=<ES password>` to the docker-compose.yml `env` section.
* The `docker-compose.yml` mounts the directory `es-dashboards/data/index` into the ES container (to save the ES index over container restarts). However, you might want to reset this (`rm -rf es-dashboards/data/index`) if you change the docker container version.
* If there is a major problem with ES (i.e. GeoNetwork cannot connect to it), then the main GeoNetwork search webpage will display a yellow box saying "No search service available currently!".  Suggestion is to go to http://localhost:9200/_cluster/health/ and http://localhost:9200/_cluster/allocation/explain for more details.  GeoNetwork might give more information from http://localhost:8800/geonetwork/criticalhealthcheck and http://localhost:8800/geonetwork/warninghealthcheck
* You may go to the search website and see a red pop-up saying "Query returned an error. Check the console for details.".  If this happens, open the Browser's devtools, and reload the page.  Look at the Network and the Console for some details.  This can be caused when there are no records in the ElasticSearch index - try to add some records to GeoNetwork and delete-and-reindex:
   * When you first run GeoNetwork (with no records in the Geonetwork Index), you will get an ElasticSearch Query error on the main GeoNetwork search HTML page.  This is normal - add more records to your GeoNetwork (i.e. go to "Admin Console" -> "Metadata and templates" and load the iso19139 samples)
   * Use the admin tools to delete and re-index metadata (i.e. go to "Admin Console" -> "Tools" and then "Delete Index and Reindex")




