#!/usr/bin/env bash

export JETTY_HOME=../jetty
export JETTY_FOREGROUND=0
export JETTY_BASE=$JETTY_HOME
cd $JETTY_HOME

for i in "$@"
do
case $i in
    -f*)
    JETTY_FOREGROUND=1
    shift
    ;;
    *)
    ;;
esac
done

rm logs/*request.log*
rm logs/output.log
mv logs/geonetwork.log.* logs/archive
mv logs/geoserver.log.* logs/archive

# Set custom data directory location using system property
#export geonetwork_dir=/app/geonetwork_data_dir
#export geonetwork_lucene_dir=/ssd/geonetwork_lucene_dir

export JAVA_MEM_OPTS="-Xms512m -Xmx1g"
# try changing the Xmx parameter if your machine has little RAM
#export JAVA_MEM_OPTS="-Xms48m -Xmx512m"

export JAVA_OPTS="$JAVA_MEM_OPTS -Xss2M -Djeeves.filecharsetdetectandconvert=enabled -Dmime-mappings=../web/geonetwork/WEB-INF/mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork"

# Set custom data directory location using Java property
# export JAVA_OPTS="$JAVA_OPTS -Dgeonetwork.dir=/app/geonetwork_data_dir -Dgeonetwork.lucene.dir=/ssd/geonetwork_lucene_dir"

export JETTY_CMD="$JAVA_OPTS -jar $JETTY_HOME/start.jar"

if [ $JETTY_FOREGROUND = 1 ];
then java $JETTY_CMD
else java $JETTY_CMD > logs/output.log 2>&1 &
fi
