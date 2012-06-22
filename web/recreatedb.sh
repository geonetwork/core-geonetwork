#!/bin/sh
set -x

if [ "jeichar" = "`whoami`" ]; then
  DB=geocat2_trunk
  SQL_DIR=/usr/local/share/postgis/
elif [ "fvanderbiest" = "`whoami`" ]; then
  DB=geocat
  SQL_DIR=/usr/share/postgresql/8.4/contrib/postgis-1.5/
else
  echo "request a target parameter: ./recreatedb.sh <username>"
  exit 1
fi

export PG_PASSWORD="www-data"
dropdb $DB
createdb -O www-data $DB

psql -d $DB -f $SQL_DIR/postgis.sql
psql -d $DB -f $SQL_DIR/spatial_ref_sys.sql

rm -rf $HOME/gc_data
mkdir -p $HOME/gc_data/config/
cp -R src/main/webapp/WEB-INF/data/codelist $HOME/gc_data/config/
cp -R src/main/webapp/WEB-INF/data/config/schema_plugins $HOME/gc_data/config/


#psql -d $DB -c "ALTER TABLE geometry_columns OWNER TO \"www-data\";"
#psql -d $DB -c "ALTER TABLE spatial_ref_sys OWNER TO \"www-data\";"

#for f in `ls sql/*.sql` ; do psql -f $f -d $DB -U www-data > /dev/null; done

#psql -f geometry_columns.sql -d $DB -U www-data > /dev/null
