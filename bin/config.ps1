$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition

$WEB_DIR="$scriptPath\..\web"
$JEEVES_DIR="$scriptPath\..\jeeves"


##################################################################################################
#
# START Parameter Section
#
##################################################################################################

##################################################################################################
# The parameters below can be overridden in your personal
# configuration file if the default don't work in your environment
##################################################################################################

# parameters passed to maven to enable debugging.  By default port 5005 will be the debug port
$DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# The overrides configuration file to use to configure your instance
$OVERRIDES="-Dgeonetwork.jeeves.configuration.overrides.file=$WEB_DIR\dev-config\override-config-dev-default.xml"

# The memory settings used when running server
$MEMORY="-XX:MaxPermSize=256m -Xmx1024M -server"

# The directory that will be configured by setup scripts to be used as the Geonetwork datadirectory
# It should be empty (or not yet exist)
# IMPORTANT clean.sh will delete the directory so make sure it is empty
$DATA_DIR="$HOME\gc_data"

# The logging configuration to use.  log4j-jeichar.cfg logs virtually everything so you might want log4j-fgravin to reduce log noise
$LOGFILE="file:///$WEB_DIR\dev-config\log4j-jeichar.cfg"

# The logging parameters passed to maven.  Will be the log configuration used in Geonetwork
$LOGGING="-Dlog4j.debug=true -Dlog4j.configuration=$LOGFILE"

# The database to create/configure and use.  The name also needs to be in the overrides file used
$DB="geocat2_trunk"

# The directory containing the postgis installation scripts.  For postgis 1.x it will contain postgis.sql and spatial_ref_sys.sql
$SQL_DIR="C:\Program Files\PostgreSQL\9.2\share\contrib\postgis-2.0\"

# A flag indicating how to create the postgis database.  You can look at clean.sh for the
# implementation.  
# At time of this writing the options are: "template" or "POSTGIS 2 create" or "POSTGIS 1 create"
$POSTGIS_INSTALL="template"

# password of www-data user...  May not be needed
$DB_PASSWORD="www-data"

$postgis_template="postgis"
##################################################################################################
#
# END Parameter Section
#
##################################################################################################


# Load personal config file
$CONFIG_FILE="$scriptPath\config_$Env:Username`.ps1"
If (Test-Path $CONFIG_FILE -PathType Leaf) 
{
	echo "Loading configuration parameters from $CONFIG_FILE"
    . "$CONFIG_FILE"
} else 
{
	echo "Configuration file: $CONFIG_FILE does not exist.  Skipping system specific configuration file"
}