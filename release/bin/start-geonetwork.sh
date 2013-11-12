cd ../jetty
rm logs/*request.log*
rm logs/output.log
mv logs/geonetwork.log.* logs/archive
mv logs/geoserver.log.* logs/archive

export JETTY_HOME=.

# Set custom data directory location using system property
#export geonetwork_dir=/app/geonetwork_data_dir
#export geonetwork_lucene_dir=/ssd/geonetwork_lucene_dir

# try changing the Xmx parameter if your machine has little RAM
#java -Xms48m -Xmx256m -Xss2M -XX:MaxPermSize=128m -Djeeves.filecharsetdetectandconvert=enabled -Dmime-mappings=../web/geonetwork/WEB-INF/mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ../bin/jetty.xml &

# Set custom data directory location using Java property
# java -Xms48m -Xmx512m -Xss2M -XX:MaxPermSize=128m -Dgeonetwork.dir=/app/geonetwork_data_dir -Dgeonetwork.lucene.dir=/ssd/geonetwork_lucene_dir -Djeeves.filecharsetdetectandconvert=enabled -Dmime-mappings=../web/geonetwork/WEB-INF/mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar

java -Xms48m -Xmx512m -Xss2M -XX:MaxPermSize=128m -Djeeves.filecharsetdetectandconvert=enabled -Dmime-mappings=../web/geonetwork/WEB-INF/mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar > logs/output.log 2>&1 &
