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
	$TRANSIFEX_DIR/translation_converter -from "$TRANSIFEX_DIR/../$1.xml" -to "$TRANSIFEX_DIR/transifex-format/$1.json" -format "$2"
}

set -x
convert web/src/main/webapp/loc/eng/xml/formatter simple
convert schemas/iso19139/src/main/plugin/iso19139/formatter/loc/eng/strings simple