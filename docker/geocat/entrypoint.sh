#!/bin/bash

set -e

ELASTICDUMP_CONFIG_FILE=/etc/datasync/elasticdump-config
if [[ ! -f "${ELASTICDUMP_CONFIG_FILE}" ]]; then
  mkdir -p /etc/datasync
  CURRENT_DATE_TIME=$(date -u -Is)
  cat << EOF > "${ELASTICDUMP_CONFIG_FILE}"
# This file has been generated automatically at ${CURRENT_DATE_TIME}
# using ELASTICDUMP_SOURCE_URL and ELASTICDUMP_TARGET_URL environment variables.

INTERNAL_ES_INDEX=${ELASTICDUMP_SOURCE_URL}
EXTERNAL_ES_INDEX=${ELASTICDUMP_TARGET_URL}

EOF
fi

PGSYNC_CONFIG_FILE=/etc/datasync/pgsync.yml
if [[ ! -f "${PGSYNC_}" ]]; then
  mkdir -p /etc/datasync
  CURRENT_DATE_TIME=$(date -u -Is)
  cat << EOF > ${PGSYNC_CONFIG_FILE}
# This file has been generated automatically at ${CURRENT_DATE_TIME}

# source database URL
# database URLs take the format of:
#   postgres://user:password@host:port/dbname
#
# we recommend a command which outputs a database URL
# so sensitive information is not included in this file
#
# we *highly recommend* you use sslmode=verify-full when possible
# see https://ankane.org/postgres-sslmode-explained for more info
from: postgres://${PGSYNC_SOURCE_USER}:${PGSYNC_SOURCE_PASSWORD}@${PGSYNC_SOURCE_HOST}:${PGSYNC_SOURCE_PORT}/${PGSYNC_SOURCE_DBNAME}

# destination database URL
to: postgres://${PGSYNC_TARGET_USER}:${PGSYNC_TARGET_PASSWORD}@${PGSYNC_TARGET_HOST}:${PGSYNC_TARGET_PORT}/${PGSYNC_TARGET_DBNAME}

groups:
  metadata:
    metadata: "where uuid = '{1}'"
    operationallowed: "where metadataid in (select id from metadata where uuid = '{1}')"
    metadataindicator:  "where metadata_id in (select id from metadata where uuid = '{1}')"

# exclude tables
# exclude:
#   - table1
#   - table2

# define groups
# groups:
#   group1:
#     - table1
#     - table2

# sync specific schemas
# schemas:
#   - public

# protect sensitive information
#data_rules:
#  email: unique_email
#  phone: unique_phone
#  last_name: random_letter
#  birthday: random_date
#  encrypted_*: null

EOF

fi


if [[ "$1" = catalina.sh ]]; then
    # this is a command to run tomcat
    echo "JAVA_OPTS=${JAVA_OPTS}"
    echo "================================="
    echo "CATALINA_OPTS=${CATALINA_OPTS}"
    echo "================================="
    echo ""
    echo ""

    # Sanity check: ES_HOST variable is mandatory
    if [ -z "${ES_HOST}" ]; then
        cat >&2 <<- EOWARN
			********************************************************************
			WARNING: Environment variable ES_HOST is mandatory
			GeoNetwork requires an Elasticsearch instance to store the index.
			Please define the variable ES_HOST with the Elasticsearch
			host name. For example
			docker run -e ES_HOST=elasticsearch geonetwork:${GN_VERSION}
			********************************************************************
		EOWARN
        exit 2
    fi;

    # Set Elasticsearch properties
    if [ "${ES_HOST}" != "localhost" ]; then
        sed -i "s#http://localhost:9200#${ES_PROTOCOL:="http"}://${ES_HOST}:${ES_PORT:="9200"}#g" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/web.xml" ;
        sed -i "s#es.host=localhost#es.host=${ES_HOST}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi;

    if [ -n "${ES_PROTOCOL}" ] && [ "${ES_PROTOCOL}" != "http" ] ; then
        sed -i "s#es.protocol=http#es.protocol=${ES_PROTOCOL}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi

    if [ -n "${ES_PORT}" ] && [ "$ES_PORT" != "9200" ] ; then
        sed -i "s#es.port=9200#es.port=${ES_PORT}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi

    if [ -n "${ES_INDEX_RECORDS}" ] && [ "$ES_INDEX_RECORDS" != "gn-records" ] ; then
        sed -i "s#es.index.records=gn-records#es.index.records=${ES_INDEX_RECORDS}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi

    if [ "${ES_USERNAME}" != "" ] ; then
        sed -i "s#es.username=#es.username=${ES_USERNAME}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi

    if [ "${ES_PASSWORD}" != "" ] ; then
        sed -i "s#es.password=#es.password=${ES_PASSWORD}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
    fi

    if [ -n "${KB_URL}" ] && [ "$KB_URL" != "http://localhost:5601" ]; then
        sed -i "s#kb.url=http://localhost:5601#kb.url=${KB_URL}#" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/config.properties" ;
        sed -i "s#http://localhost:5601#${KB_URL}#g" "${CATALINA_HOME}/webapps/geonetwork/WEB-INF/web.xml" ;
    fi

    exec "$@"

else
  exec "$@"
fi
