#!/bin/sh

# resolve links - so that script can be called from any dir
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`pwd`/`dirname "$PRG"`

. $PRGDIR/config.sh

rm -rf $DATA_DIR/config/schema_plugins
ln -s $WEB_DIR/src/main/webapp/WEB-INF/data/config/schema_plugins $DATA_DIR/config/schema_plugins