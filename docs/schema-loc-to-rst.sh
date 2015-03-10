#!/bin/bash

GNLIB=../web/target/geonetwork/WEB-INF/lib/

function showUsage
{
  echo -e "\nThis script is used to create a RST file for schema elements"
  echo
  echo -e "Usage: ./`basename $0 $1` iso19139 eng"
}

if [ "$1" = "-h" ]
then
        showUsage
        exit
fi


if [ $# -lt 2 ]
then
  showUsage
  exit
fi


java -classpath $GNLIB/saxon-9.1.0.8b-patch.jar net.sf.saxon.Transform \
        -s:build.xml \
        -xsl:schema-loc-to-rst.xsl \
        -o:schema-loc-$1-$2.rst \
        lang=$2 \
        schema=$1