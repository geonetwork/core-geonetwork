#!/bin/bash

gnusername="$1"
gnpassword="$2"
gnnodeid="$3"
gnwebappname="geosource"
gninstalldir="/applications/geosource"
gndburl="$4"
gndbdriver="$5"
gndbdriver_default="postgres"
gnpoolsize="$6"
gnpoolsize_default=2
gnminIdle=0
gnmaxIdle=$gnpoolsize


# Set default values
if [ -z "$gndbdriver" ]
then
  gndbdriver=$gndbdriver_default
fi
if [ -z "$gnpoolsize" ]
then
  gnpoolsize=$gnpoolsize_default
fi

# When running from the source code
#GNLIB=../../../../../target/geonetwork/WEB-INF/lib/
#WEB_FILE=../../../webResources/WEB-INF/web.xml
#WEB_FILE_OUT=../../../webapp/WEB-INF/web.xml
# When running from the app
GNLIB=../lib/
WEB_FILE=../web.xml
WEB_FILE_OUT=../web.xml

function showUsage 
{
  echo -e "\nThis script is used to create a new node configuration" 
  echo -e "\n  Default pool size: $gnpoolsize_default" 
  echo -e "  Default db driver: $gndbdriver_default"
  echo -e "  Default webapp path: $GEONETWORK_HOME"
  echo
  echo -e "Usage: ./`basename $0 $1` username password nodeid dburl"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver dbpoolsize"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver dbpoolsize webapppath"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb postgres "
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb postgres 5"
  echo -e "\t./`basename $0 $1` admin gnos 42 jdbc:h2:/tmp/geonetwork42 h2 2"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb postgres 2"
  echo
}

if [ "$1" = "-h" ]
then
        showUsage
        exit
fi


if [ $# -lt 4 ]
then
  showUsage
  exit
fi
echo "Creating node  : $gnnodeid"
echo "      DB driver: $gndbdriver"
echo "      DB URL   : $gndburl"
echo "      DB user  : $gnusername"

# TODO: could be nice to add removal action too

echo "Register node in web.xml ..."

java -classpath $GNLIB/xalan-2.7.1.jar:$GNLIB/serializer-2.7.1.jar org.apache.xalan.xslt.Process \
        -PARAM user $gnusername \
        -PARAM password $gnpassword \
        -PARAM nodeId $gnnodeid \
        -PARAM dbDriver $gndbdriver \
        -PARAM dbUrl $gndburl \
        -PARAM poolSize $gnpoolsize \
        -IN $WEB_FILE -XSL register-node.xsl \
        -OUT $WEB_FILE_OUT
mv $wEB_FILE $WEB_FILE.bak
mv $wEB_FILE_OUT $WEB_FILE

echo "Setting db connection and SPRING configuration ..."
java -classpath $GNLIB/xalan-2.7.1.jar:$GNLIB/serializer-2.7.1.jar org.apache.xalan.xslt.Process \
        -PARAM user $gnusername \
        -PARAM password $gnpassword \
        -PARAM nodeId $gnnodeid \
        -PARAM dbDriver $gndbdriver \
        -PARAM dbUrl $gndburl \
        -PARAM poolSize $gnpoolsize \
        -IN ../config-node/srv.xml -XSL generate-spring-config.xsl \
        -OUT ../config-node/$gnnodeid.xml

sed -i s/j2e://g $WEB_FILE




