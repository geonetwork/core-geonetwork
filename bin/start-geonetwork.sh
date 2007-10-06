cd ../jetty
rm log/*request.log*
mv log/jeeves.log.* log/archive
rm log/jetty.log

# try changing the Xmx parameter if your machine has little RAM
#java -Xms48m -Xmx256m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml &

java -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ../bin/jetty.xml > log/jetty.log 2>&1 &
