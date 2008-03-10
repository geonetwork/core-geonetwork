cd ../jetty
rm log/*request.log*
rm log/output.log
mv log/geonetwork.log.* log/archive
mv log/intermap.log.*   log/archive

# try changing the Xmx parameter if your machine has little RAM
#java -Xms48m -Xmx256m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml &

java -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml > log/output.log 2>&1 &
