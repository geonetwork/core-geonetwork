#!/bin/sh

set -x

. config.sh

if [ -z "$JREBEL_HOME" ] ; then
    echo "you do not have JREBEL installed.  If you want to use JREBEL install it and define JREBEL_HOME.  It can be defined in the config.sh file in your profile"
else
  JREBEL_OPTS="-noverify -javaagent:$JREBEL_HOME/jrebel.jar"
fi

export MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY -Dgeonetwork.dir=$DATA_DIR "

cd $JEEVES_DIR
mvn install $@
if [ ! $? -eq 0 ]; then
    echo "[FAILURE] [deploy] Failed to execute 'jeeves' correctly"
    exit -1
fi

cd $WEB_DIR
mvn jetty:run -Penv-dev $@