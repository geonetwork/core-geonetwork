#!/bin/bash
set -e

if [ "$1" = 'catalina.sh' ]; then

    if [ ! -d "$DATA_DIR" ]; then
        echo "$Data directory '$DATA_DIR' does not exist. Creating it..."
        mkdir -p "$DATA_DIR"
    fi

    #Set geonetwork data dir
    export CATALINA_OPTS="$CATALINA_OPTS -Dgeonetwork.dir=$DATA_DIR"

    #Setting host (use $POSTGRES_DB_HOST if it's set, otherwise use "postgres")
    db_host="${POSTGRES_DB_HOST:-postgres}"
    echo "db host: $db_host"

    #Setting port
    db_port="${POSTGRES_DB_PORT:-5432}"
    echo "db port: $db_port"

    if [ -z "$POSTGRES_DB_USERNAME" ] || [ -z "$POSTGRES_DB_PASSWORD" ]; then
        echo >&2 "you must set POSTGRES_DB_USERNAME and POSTGRES_DB_PASSWORD"
        exit 1
    fi

    db_gn="${POSTGRES_DB_NAME:-geonetwork}"

    #Write connection string for GN
    sed -ri '/^jdbc[.](username|password|database|host|port)=/d' "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties
    echo "jdbc.username=$POSTGRES_DB_USERNAME" >> "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties
    echo "jdbc.password=$POSTGRES_DB_PASSWORD" >> "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties
    echo "jdbc.database=$db_gn" >> "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties
    echo "jdbc.host=$db_host" >> "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties
    echo "jdbc.port=$db_port" >> "$CATALINA_HOME"/webapps/geonetwork/WEB-INF/config-db/jdbc.properties

    #Fixing an hardcoded port on the connection string (bug fixed on development branch)
    sed -i -e 's#5432#${jdbc.port}#g' $CATALINA_HOME/webapps/geonetwork/WEB-INF/config-db/postgres.xml

    # Reconfigure Elasticsearch & Kibana if necessary
    if [ "$ES_HOST" != "localhost" ]; then
      sed -i "s#http://localhost:9200#${ES_PROTOCOL}://${ES_HOST}:${ES_PORT}#g" $CATALINA_HOME/webapps/geonetwork/WEB-INF/web.xml ;
      sed -i "s#es.url=http://localhost:9200#es.url=${ES_PROTOCOL}://${ES_HOST}:${ES_PORT}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
    fi;

    if [ "$ES_USERNAME" != "" ] ; then
      sed -i "s#es.username=#es.username=${ES_USERNAME}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
    fi
    if [ "$ES_PASSWORD" != "" ] ; then
      sed -i "s#es.password=#es.password=${ES_PASSWORD}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
    fi

    if [ "$ES_INDEX_SEARCHLOGS" != "gn-searchlogs" ] ; then
      sed -i "s#es.index.searchlogs=gn-searchlogs#es.index.searchlogs=${ES_INDEX_SEARCHLOGS}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
    fi

    if [ "$ES_INDEX_RECORDS" != "gn-records" ] ; then
      sed -i "s#es.index.records=gn-records#es.index.records=${ES_INDEX_RECORDS}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
      sed -i "s#gn-records#${ES_INDEX_RECORDS}#g" $CATALINA_HOME/webapps/geonetwork/WEB-INF/web.xml ;
    fi

    if [ "$ES_INDEX_FEATURES" != "gn-features" ] ; then
      sed -i "s#es.index.features=gn-features#es.index.features=${ES_INDEX_FEATURES}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
      sed -i "s#gn-features#${ES_INDEX_FEATURES}#g" $CATALINA_HOME/webapps/geonetwork/WEB-INF/web.xml ;
    fi

    if [ "$KB_URL" != "http://localhost:5601" ]; then
      sed -i "s#kb.url=http://localhost:5601#kb.url=${KB_URL}#" $CATALINA_HOME/webapps/geonetwork/WEB-INF/config.properties ;
      sed -i "s#http://localhost:5601#${KB_URL}#g" $CATALINA_HOME/webapps/geonetwork/WEB-INF/web.xml ;
    fi

fi

exec "$@"

