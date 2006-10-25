cd ../jetty
rm log/*request.log*
mv log/jeeves.log.* log/archive

java -Xmx512m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml
