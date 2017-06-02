#!/bin/bash

gnusername="$1"
gnpassword="$2"
gnnodeid="$3"
gnwebappname="geosource"
gninstalldir="/applications/geosource"
gnhost="$4"
gndburl="$5"
gndbdriver="$6"
gndbdriver_default="postgres"
gnpoolsize="$7"
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

if [ -d ../../../webResources/WEB-INF ]
then
  # When running from the source code
  echo -e "Running from the source code"
  GNLIB=../../../../../target/geonetwork/WEB-INF/lib/
  CONFIGNODE_FOLDER=../../../webResources/WEB-INF/config-node
  WEB_FILE=../../../webResources/WEB-INF/web.xml
  WEB_FILE_OUT=../../../webapp/WEB-INF/web.xml
  CSS_FILE=../../../../../target/geonetwork/catalog/style/${gnnodeid}_custom_style.css
else
  # When running from the app
  echo -e "Running from the app"
  GNLIB=../lib/
  CONFIGNODE_FOLDER=../config-node
  WEB_FILE=../web.xml
  WEB_FILE_OUT=../web-temp.xml
  CSS_FILE=../../catalog/style/${gnnodeid}_custom_style.css
fi

function showUsage
{

  echo -e "\nThis script is used to create a new node configuration"
  echo -e "\n  Default pool size: $gnpoolsize_default"
  echo -e "  Default db driver: $gndbdriver_default"
  echo -e "  Default webapp path: $GEONETWORK_HOME"
  echo
  echo -e "Usage: ./`basename $0 $1` username password nodeid nodehost dburl"
  echo -e "       ./`basename $0 $1` username password nodeid nodehost dburl dbdriver"
  echo -e "       ./`basename $0 $1` username password nodeid nodehost dburl dbdriver dbpoolsize"
  echo -e "       ./`basename $0 $1` username password nodeid nodehost dburl dbdriver dbpoolsize webapppath"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0 $1` admin admin 42 www.node42.com jdbc:postgresql://localhost:5432/catdb"
  echo -e "\t./`basename $0 $1` admin admin 42 www.node42.com jdbc:postgresql://localhost:5432/catdb postgres "
  echo -e "\t./`basename $0 $1` admin admin 42 www.node42.com jdbc:postgresql://localhost:5432/catdb postgres 5"
  echo -e "\t./`basename $0 $1` admin gnos 42 www.node42.com jdbc:h2:/tmp/geonetwork42 h2 2"
  echo -e "\t./`basename $0 $1` admin admin 42 www.node42.com jdbc:postgresql://localhost:5432/catdb postgres 2"
  echo
}

if [ "$1" = "-h" ]
then
        showUsage
        exit
fi


if [ $# -lt 5 ]
then
  showUsage
  exit
fi
echo "Creating node  : $gnnodeid"
echo "      host     : $gnhost"
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
        -PARAM host $gnhost \
        -IN $WEB_FILE -XSL register-node.xsl \
        -OUT $WEB_FILE_OUT
mv $WEB_FILE $WEB_FILE.bak
mv $WEB_FILE_OUT $WEB_FILE

echo "Setting db connection and SPRING configuration ..."
java -classpath $GNLIB/xalan-2.7.1.jar:$GNLIB/serializer-2.7.1.jar org.apache.xalan.xslt.Process \
        -PARAM user $gnusername \
        -PARAM password $gnpassword \
        -PARAM nodeId $gnnodeid \
        -PARAM dbDriver $gndbdriver \
        -PARAM dbUrl $gndburl \
        -PARAM poolSize $gnpoolsize \
        -IN $CONFIGNODE_FOLDER/srv.xml -XSL generate-spring-config.xsl \
        -OUT $CONFIGNODE_FOLDER/$gnnodeid.xml

# Replace j2e: by empty string in web.xml
if [ "$(uname)" == "Darwin" ]
then
  sed -i  .bak s/j2e://g $WEB_FILE
else
  sed -i s/j2e://g $WEB_FILE
fi

# Create CSS file if doesn't exists
if [ ! -f $CSS_FILE ];
then
   echo "File $CSS_FILE doesn't exists, creating it ..."
   touch ${CSS_FILE}
fi

echo "Node created."




