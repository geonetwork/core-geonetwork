cd ../jetty
rm logs/*request.log*
rm logs/output.log
mv logs/geonetwork.log.* logs/archive
mv logs/intermap.log.*   logs/archive
mv logs/geoserver.log.* logs/archive

# try changing the Xmx parameter if your machine has little RAM
#java -Xms48m -Xmx256m -Xss2M -XX:MaxPermSize=128m -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ../bin/jetty.xml &

java -Xms48m -Xmx512m -Xss2M -XX:MaxPermSize=128m -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ../bin/jetty.xml > logs/output.log 2>&1 &
