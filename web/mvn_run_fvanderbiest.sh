#!/bin/sh


DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
OVERRIDES="-Dgeonetwork.jeeves.configuration.overrides.file=/WEB-INF/override-config-fvanderbiest.xml"
MEMORY="-XX:MaxPermSize=256m -Xmx1024M -server"
DIRS="-Dgeonetwork.dir=$HOME/gc_data"
export MAVEN_OPTS="$JREBEL_OPTS $DEBUG $OVERRIDES $MEMORY $DIRS -Dfile.encoding=UTF8 "

cd ../jeeves 
mvn3 install -Dmaven.test.skip
if [ ! $? -eq 0 ]; then
    echo "[FAILURE] [deploy] Failed to execute 'jeeves' correctly"
    exit -1
fi
cd ../web
mvn3 jetty:run -Penv-dev,widgets-tab $@