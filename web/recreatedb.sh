#!/bin/sh
set -x

if [ "jeichar" = "`whoami`" ]; then
  DB=geocat2_trunk
  SQL_DIR=/usr/local/share/postgis/
elif [ "fvanderbiest" = "`whoami`" ]; then
  DB=geocat
  SQL_DIR=/usr/local/share/postgis
else
  echo "request a target parameter: ./recreatedb.sh <username>"
  exit 1
fi

export PG_PASSWORD="www-data"
dropdb $DB
createdb -O www-data $DB

psql -d $DB -c "CREATE EXTENSION postgis;"
psql -d $DB -c "CREATE EXTENSION postgis_topology;"
psql -d $DB -f $SQL_DIR/legacy.sql

rm -rf $HOME/gc_data
mkdir -p $HOME/gc_data/config/
cp -R src/main/webapp/WEB-INF/data/config/codelist $HOME/gc_data/config/
cp -R src/main/webapp/WEB-INF/data/config/schema_plugins $HOME/gc_data/config/

