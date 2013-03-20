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
$DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006"

# The memory settings used when running server
$MEMORY="-XX:MaxPermSize=256m -Xmx1024M -server"

# The directory that will be configured by setup scripts to be used as the Geonetwork datadirectory
# It should be empty (or not yet exist)
# IMPORTANT clean.sh will delete the directory so make sure it is empty
$DATA_DIR="$scriptPath\..\datadir"

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

$LOG = ".\web\geonetwork.log"
If (Test-Path $LOG -PathType Leaf) 
{
	rm $LOG
}