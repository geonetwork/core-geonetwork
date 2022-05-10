Configuration
=============

Environment variables
---------------------

* **`ELASTICDUMP_SOURCE_URL`**: The URL of the Elasticsearch index used as source, usually by the GeoNetwork internal 
instance. For example `http://elasticsearch:9200/gn-records`
* **`ELASTICDUMP_TARGET_URL`**: The URL of the Elasticsearch index where the records are going to be copied to, usually
used by GeoNetwork external instance. For example `http://elasticsearch:9200/gn-records-external`
* **`PGSYNC_SOURCE_USER`**: username used for connecting to the pgsync source database.
* **`PGSYNC_SOURCE_PASSWORD`**: password used for connecting to the pgsync source database.
* **`PGSYNC_SOURCE_HOST`**: hostname of the pgsync source database.
* **`PGSYNC_SOURCE_PORT`**: port number of the pgsync source database.
* **`PGSYNC_SOURCE_DBNAME`**: database name of the pgsync source database.
* **`PGSYNC_TARGET_USER`**: username used for connecting to the pgsync target database.
* **`PGSYNC_TARGET_PASSWORD`**: password used for connecting to the pgsync target database.
* **`PGSYNC_TARGET_HOST`**: hostname of the pgsync target database.
* **`PGSYNC_TARGET_PORT`**: port number of the pgsync target database.
* **`PGSYNC_TARGET_DBNAME`**: database name of the pgsync target database.
