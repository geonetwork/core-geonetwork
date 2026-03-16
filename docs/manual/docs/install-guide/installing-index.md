# Installing search platform

The GeoNetwork search engine is built on top of Elasticsearch. The platform is used to index records and also to index WFS data (See [Analyze and visualize data](../user-guide/analyzing/data.md) ).

GeoNetwork requires an [Elasticsearch](https://www.elastic.co/products/elasticsearch) instance to be installed next to the catalog.


## Elasticsearch compatibility

Elasticsearch Java client version: 8.14.3

| Elasticsearch Version | Compatibility |
|-----------------------| ------------- |
| Elasticsearch 8.14.3  | recommended   |
| Elasticsearch 8.14.x  | minimum       |

Older version may be supported but are untested.


## Installation

=== "Manual installation"
        
    1. **Download:** Elasticsearch `8.14.3` from <https://www.elastic.co/downloads/elasticsearch> and unzip the file.

        ``` shell
        wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.14.3.tar.gz
        tar xvfz elasticsearch-8.14.3.tar.gz
        ```

    2. **Start**: Manually start Elasticsearch using:

        ``` shell
        elasticsearch-8.14.3/bin/elasticsearch
        ```

    3. **Stop**: Manually stop Elasticsearch using:

        ``` shell
        elasticsearch-8.14.3/bin/elasticsearch stop
        ```
        
=== "Install using Maven"

    1. Developers are encouraged to run Elasticseach using maven in order to test against the version of Elasticsearch intended for the next release.
    
        !!! note
            
            When running from source code the Elasticsearch version is obtained from [pom.xml](https://github.com/geonetwork/core-geonetwork/blob/main/pom.xml) property `es.version`.
    
    2. **Download**: Run maven from the **`es`** directory.

          ``` shell
          cd es
          mvn install -Pes-download
          ```
    
    3. **Start**: Use maven ``exec`` plugin to run Elasticsearch:
    
        ``` shell
        mvn exec:exec -Des-start
        ```
        
        Elasticsearch will run in foreground mode sending output to the shell.

    4. **Stop**: To stop Elasticsearch use ++ctrl+c++ to stop the maven process.

## Index creation

1. GeoNetwork will connect to Elasticsearch on startup, and indices will be created if they do not exist.
   
    * GeoNetwork will start even if Elasticsearch index is down (or not yet running).
    
    * When GeoNetwork cannot contact Elasticsearch a warning will be displayed.
    
    * When Elasticsearch index is available, indices are created if they do not exist.

2. Optional: To manually create indices use the **Admin Console** to create the default index.
   
    The application will create indices automatically once the Elasticsearch is up and running and if no indices are found.

## Customizing index

1. User may want to customize the index settings for example to change language configuration (see [es/README.md](https://github.com/geonetwork/core-geonetwork/tree/main/es#readme)).

2. Check the configuration file in **`$GN_DATA_DIRECTORY/config/index`**.

3. To manually remove and recreate the index, use the following:
    
    ``` shell
    cd $GN_DATA_DIRECTORY/config/index
    curl -X DELETE http://localhost:9200/gn-features
    curl -X DELETE http://localhost:9200/gn-records
    curl -X DELETE http://localhost:9200/gn-searchlogs
    
    curl -X PUT http://localhost:9200/gn-features -H 'Content-Type: application/json' -d @features.json
    curl -X PUT http://localhost:9200/gn-records -H 'Content-Type: application/json' -d @records.json
    curl -X PUT http://localhost:9200/gn-searchlogs -H 'Content-Type: application/json' -d @searchlogs.json
    ```

## Check Elasticsearch installation

1. Access Elasticsearch admin page from <http://localhost:9200/>.

    !!! note
    
        It is not needed nor recommended to open port `9200` to the outside. GeoNetwork is protecting the Elasticsearch instance exposing only the search API and taking care of user privileges.

## Configure GeoNetwork connection to Elasticsearch

By default, GeoNetwork expects Elasticsearch to be running at <http://localhost:9200> without authentication. If your Elasticsearch server is on a different host or port or requires authentication, you will need to configure connection details using either of these methods:

* Define the connection details in Java properties.

  ```shell
  export JAVA_OPTS="$JAVA_OPTS -Des.protocol=http -Des.port=9200 -Des.host=localhost  -Des.protocol=http -Des.username= -Des.password="
  ```

* Define the connection details in environment variables.

  ```shell
  export GEONETWORK_ES_HOST=localhost
  export GEONETWORK_ES_PROTOCOL=http
  export GEONETWORK_ES_PORT=9200
  export GEONETWORK_ES_USERNAME=
  export GEONETWORK_ES_PASSWORD=
  ```

* Edit the values in ```WEB-INF/config.properties``` (not recommended):

  ```properties
  es.protocol=#{systemEnvironment['GEONETWORK_ES_PROTOCOL']?:'http'}
  es.port=#{systemEnvironment['GEONETWORK_ES_PORT']?:9200}
  es.host=#{systemEnvironment['GEONETWORK_ES_HOST']?:'localhost'}
  es.username=#{systemEnvironment['GEONETWORK_ES_USERNAME']?:''}
  es.password=#{systemEnvironment['GEONETWORK_ES_PASSWORD']?:''}
  ```

Once the configuration is complete, you will need to restart the application.
