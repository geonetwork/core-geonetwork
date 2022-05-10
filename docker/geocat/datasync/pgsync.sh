#!/bin/bash

if [[ $# == 0 ]]; then
  echo "Copy records from one table of a database to another "
  echo "Usage:"
  echo ""
  echo "pgsync.sh harvesterUUID"
  echo ""
  exit 1
fi



pgsync --config /etc/datasync/pgsync.yml metadata:"${1}"
