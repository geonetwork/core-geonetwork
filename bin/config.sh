#!/bin/sh

WEB_DIR="$PRGDIR/../web"
JEEVES_DIR="$PRGDIR/../jeeves"


#################################################
# The parameters below can be overridden in your personal
# configuration file if the default don't work
#################################################
DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
OVERRIDES="-Dgeonetwork.jeeves.configuration.overrides.file=$WEB_DIR/dev-config/override-config-dev-default.xml"
MEMORY="-XX:MaxPermSize=256m -Xmx1024M -server"
DATA_DIR="$HOME/gc_data"
LOGFILE="file://$WEB_DIR/dev-config/log4j-jeichar.cfg"
LOGGING="-Dfile.encoding=UTF8 -Dlog4j.debug=true -Dlog4j.configuration=file://$WEB_DIR/dev-config/log4j-jeichar.cfg"
DB="geocat2_trunk"
SQL_DIR="/usr/local/share/postgis/"
POSTGIS_INSTALL="template" # options are: "template" or "POSTGIS 2 create" or "POSTGIS 1 create"
export PG_PASSWORD="www-data"

# Load personal config file
CONFIG_FILE="config_`whoami`.sh"
. $PRGDIR/$CONFIG_FILE
