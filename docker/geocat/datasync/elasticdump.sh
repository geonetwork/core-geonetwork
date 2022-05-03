#!/bin/bash

if [[ $# == 0 ]]; then
  echo "Copy the records from the internal instance to the external instance for the "
  echo "harvester uuid passed by parameters"
  echo "Usage:"
  echo ""
  echo "elasticdump.sh harvesterUUID"
  echo ""
  exit 1
fi


if [[ -f /etc/datasync/elasticdump-config ]]; then
  # Load the environment variables from the configuration file
  source /etc/datasync/elasticdump-config

  if [[ -z "${INTERNAL_ES_INDEX}" || -z "${EXTERNAL_ES_INDEX}" ]]; then
    echo "INTERNAL_ES_INDEX or EXTERNAL_ES_INDEX are not defined in /etc/datasync/elasticdump-config"
    exit 2
  fi

else
  INTERNAL_ES_INDEX=http://localhost:9200/gn-records
  EXTERNAL_ES_INDEX=http://localhost:9200/gn-records-2
fi

elasticdump \
  --input=$INTERNAL_ES_INDEX \
  --output=$EXTERNAL_ES_INDEX \
  --searchBody="{\"query\":{\"term\":{\"harvesterUuid\": \"$1\"}}}" \
  --type=data
