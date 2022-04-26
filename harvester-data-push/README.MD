# Harvester data push module

This module is used to synchronise the metadata from the harverters to an external instance of GeoNetwork.

It is included in GeoNetwork when compiled with the profile `harvester-data-push` and only works with Postgres databases.

```
mvn clean package install -Pharvester-data-push
```

## Requirements

### Tools

Install in the server the following tools:

- elasticdump (https://github.com/elasticsearch-dump/elasticsearch-dump)
- pgsync (https://github.com/ankane/pgsync)

### Configuration

- Create a folder in the server with the following files. For example, a folder `/opt/datasynch`:

  - `elasticdump.sh` with the following content, configuring the urls of the ElasticSearch indexes:

    ```
    #!/bin/bash

     # origin ElasticSearch
    INTERNAL_ES_INDEX=http://LOCALSERVER:9200/gn-records
    # destination ElasticSearch
    EXTERNAL_ES_INDEX=http://REMOTESERVER:9200/gn-records

    elasticdump \
      --input=$INTERNAL_ES_INDEX \
      --output=$EXTERNAL_ES_INDEX \
      --searchBody="{\"query\":{\"term\":{\"harvesterUuid\": \"$1\"}}}" \
      --type=data
    ```

  - `.pgsync.yml` with the following content, configuring the connections of the Postgres databases:

    ```
    # origin database
    from: postgres://USERNAME:PASSWORD@LOCALSERVER:PORT/DATABASENAME
    # destination database
    to: postgres://USERNAME:PASSWORD@REMOTESERVER:PORT/DATABASENAME

    groups:
      metadata:
        metadata: "where harvestuuid = '{1}'"
        operationallowed: "where metadataid in (select id from metadata where harvestuuid = '{1}')"
        metadataindicator:  "where metadata_id in (select id from metadata where harvestuuid = '{1}')"
    ```

  - `pgsync.sh ` with the following content:

    ```
    #!/bin/bash

    pgsync metadata:$1
    ```

  The shell scripts must have execution permissions for the servlet container user.

- Access to the GeoNetwork settings and configure the setting `Database synchronisation tools path` with the folder path created in the previous step. For example,  `/opt/datasynch`.
