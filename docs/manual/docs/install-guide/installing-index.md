# Installing search platform

The GeoNetwork search engine is built on top of Elasticsearch. The platform is used to index records and also to index WFS data (See [Analyze and visualize data](../user-guide/analyzing/data.md) ).

GeoNetwork requires an [Elasticsearch](https://www.elastic.co/products/elasticsearch) instance to be installed next to the catalog.


## Elasticsearch compatibility

Elasticsearch Java client version: 8.19.13

| Elasticsearch Version | Compatibility |
|-----------------------| ------------- |
| Elasticsearch 8.19.13 | recommended   |
| Elasticsearch 8.14.x  | minimum       |

Older versions may be supported but are untested.


## Installation

=== "Manual installation"
        
    1. **Download:** Elasticsearch `8.19.13` from <https://www.elastic.co/downloads/elasticsearch> and unzip the file.

        ``` shell
        wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.19.13.tar.gz
        tar xvfz elasticsearch-8.19.13.tar.gz
        ```

    2. **Start**: Manually start Elasticsearch using:

        ``` shell
        elasticsearch-8.19.13/bin/elasticsearch
        ```

    3. **Stop**: Manually stop Elasticsearch using:

        ``` shell
        elasticsearch-8.19.13/bin/elasticsearch stop
        ```
        
=== "Install using Maven"

    1. Developers are encouraged to run Elasticsearch using Maven in order to test against the version of Elasticsearch intended for the next release.
    
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
    curl -X DELETE http://localhost:9200/features
    curl -X DELETE http://localhost:9200/records
    curl -X DELETE http://localhost:9200/searchlogs
    
    curl -X PUT http://localhost:9200/features -H 'Content-Type: application/json' -d @features.json
    curl -X PUT http://localhost:9200/records -H 'Content-Type: application/json' -d @records.json
    curl -X PUT http://localhost:9200/searchlogs -H 'Content-Type: application/json' -d @searchlogs.json
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


## Using semantic search with Elasticsearch

To use semantic search with Elasticsearch, one option is to run a model to compute embeddings at index and search time. 

The main benefits of the hybrid search are:
* when a lexical search does not return any results, the semantic search can still find records that are semantically related to the query terms.
* it can improve the ranking of the results by combining the lexical and semantic scores.
* it can support multilingual search by using a model that can compute embeddings for multiple languages.


### Setting up the model

This model (local or not) computes embeddings for the text fields of the records and stores them in Elasticsearch.
For the time being, only the record title is used to compute embeddings.

Then, at search time, the same model is used to compute embeddings for the query terms. Then the hybrid search combines a lexical search and a semantic (vector) search in Elasticsearch.
Computing embeddings has an overhead at index and search time, but it allows to find records that are semantically related to the query terms.

The following example uses [Ollama](https://ollama.com/) to run a local model `bge-m3` using Docker.
Create a `docker-compose.yml` file with the following content:

```yaml
services:
  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    entrypoint: [ "/bin/sh", "-c" ]
    command:
      - |
        ollama serve &
        echo "Waiting for Ollama server..."
        while ! curl -s http://localhost:11434/api/tags > /dev/null; do sleep 1; done
        echo "Pulling bge-m3 model..."
        ollama pull bge-m3
        echo "Model ready! Keeping server alive..."
        wait
volumes:
  ollama_data:
```

and then run the following command to start the Ollama server:

```shell
docker-compose up -d
```

Once the Ollama server is running, you can check the model is available with http://localhost:11434/api/tags.
Then, you can configure GeoNetwork to use it for semantic search by setting the following properties in ```WEB-INF/config.properties```:

```properties
semantic.server.url=http://localhost:11434/v1/embeddings
semantic.server.model=bge-m3
semantic.server.apikey=
```

Depending on the model you are using, you may need to change the vector dimension in the index configuration file. For example, the `bge-m3` model uses 1024 dimensions.

```json
   "properties": {
      "text_vector": {
        "type": "dense_vector",
        "dims": 1024
      }
```

Once updated, you need to recreate the index and reindex your records from the admin console.


### Searching

When searching a [KNN search](https://www.elastic.co/docs/solutions/search/vector/knn) is used in combination with the standard search.

Searching for "rivière en afrique" will return the "Hydrological basins of Africa" record even if the words "rivière" or "afrique" are not present in the record title.

Explanation of the score for this search is the following:

```json
{
  "hits": {
    "hits": [
      {
        "_id": "da165110-88fd-11da-a88f-000d939bc5d8",
        "_explanation": {
          "value": 0.7646189,
          "description": "sum of:",
          "details": [
              {
                  "value": 0.7646189,
                  "description": "sum of:",
                  "details": [
                      {
                          "value": 0.7646189,
                          "description": "within top k documents",
                          "details": []
                      }
                  ]
              },
              {
                  "value": 0.0,
                  "description": "match on required clause, product of:",
                  "details": [
                      {
                          "value": 0.0,
                          "description": "# clause",
                          "details": []
                      },
                      {
                          "value": 1.0,
                          "description": "FieldExistsQuery [field=_primary_term]",
                          "details": []
                      }
                  ]
              }
          ]
      },
```

The total score is the sum of the KNN score (0.7646189) and the standard search score (0.0). 
The KNN score is computed based on the similarity between the query embedding and the record embedding, 
while the standard search score is based on the presence of query terms in the record fields.


### Indexing

For the time being only the main language record title is used to compute embeddings (see `index.xsl` for each schema).
