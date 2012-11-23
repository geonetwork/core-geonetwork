#!/bin/sh
set -x
. config.sh
dropdb $DB

case $POSTGIS_INSTALL in
  "POSTGIS 2 create")
    createdb -O www-data $DB
    psql -d $DB -c "CREATE EXTENSION postgis;"
    psql -d $DB -c "CREATE EXTENSION postgis_topology;"
    psql -d $DB -f $SQL_DIR/legacy.sql
    ;;
  "template")
    createdb -O www-data $DB -T template_postgis
    ;;
  "POSTGIS 1 create")
    createdb -O www-data $DB
    createlang plpgsql $DB
    psql -d $DB -f $SQL_DIR/postgis.sql
    psql -d $DB -f $SQL_DIR/spatial_ref_sys.sql
    ;;
esac

rm -rf $DATA_DIR
mkdir -p $DATA_DIR/config/
cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/codelist $DATA_DIR/config/
cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/schema_plugins $DATA_DIR/config/

