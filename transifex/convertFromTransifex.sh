#!/bin/bash

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
TRANSIFEX_DIR=`pwd`/`dirname "$PRG"`

function convert {
    FILE="$TRANSIFEX_DIR/downloadedFromTransifex/$1.json"
    if [ -f $FILE ]; then
	    $TRANSIFEX_DIR/translation_converter -from "$FILE" -to "$TRANSIFEX_DIR/../$2.xml" -format "$3"
	fi
}

set -x
convert for_use_core-geonetwork_webwebapplocformatterjson_fr web/src/main/webapp/loc/fre/xml/formatter simple
convert for_use_core-geonetwork_webwebapplocformatterjson_de web/src/main/webapp/loc/ger/xml/formatter simple
convert for_use_core-geonetwork_webwebapplocformatterjson_it web/src/main/webapp/loc/ita/xml/formatter simple
convert for_use_core-geonetwork_iso19193formatterstrings_fr schemas/iso19139/src/main/plugin/iso19139/formatter/loc/fre/strings simple
convert for_use_core-geonetwork_iso19193formatterstrings_de schemas/iso19139/src/main/plugin/iso19139/formatter/loc/ger/strings simple
convert for_use_core-geonetwork_iso19193formatterstrings_it schemas/iso19139/src/main/plugin/iso19139/formatter/loc/ita/strings simple