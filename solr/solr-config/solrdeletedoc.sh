#!/bin/bash

curl http://localhost:8984/solr/catalog_srv/update \
    --data '<delete><query>*:*</query></delete>' \
    -H 'Content-type:text/xml; charset=utf-8'

curl http://localhost:8984/solr/catalog_srv/update \
    --data '<commit/>' \
    -H 'Content-type:text/xml; charset=utf-8'
