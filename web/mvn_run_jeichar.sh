#!/bin/sh

if [ -z "$JREBEL_HOME" ] ; then
    echo "you do not have JREBEL installed.  You need to define JREBEL_HOME"
    exit -1
fi

JREBEL_OPTS="-noverify -javaagent:$JREBEL_HOME/jrebel.jar"
DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
OVERRIDES="-Dgeonetwork.jeeves.configuration.overrides.file=/WEB-INF/override-config-jeichar.xml"
MEMORY="-XX:MaxPermSize=256m -Xmx1024M -server"
DIRS="-Dgeonetwork.dir=$HOME/gc_data"
export MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY $DIRS -Dfile.encoding=UTF8 -Dlog4j.debug=true -Dlog4j.configuration=file://`pwd`/src/main/webapp/WEB-INF/log4j-jeichar.cfg"

cd ../jeeves 
mvn install -Dmaven.test.skip
if [ ! $? -eq 0 ]; then
    echo "[FAILURE] [deploy] Failed to execute 'jeeves' correctly"
    exit -1
fi
cd ../web
mvn jetty:run -Penv-dev,widgets-tab $@