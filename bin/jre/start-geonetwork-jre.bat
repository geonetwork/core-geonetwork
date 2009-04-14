cd ..\jetty
del logs\*request.log*
del logs\output.log
move logs\geonetwork.log.* logs\archive
move logs\intermap.log.*   logs\archive
move logs\geoserver.log.* logs\archive

..\jre1.5.0_12\bin\java -Xms48m -Xmx512m -Xss2M -XX:MaxPermSize=128m -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ..\bin\jetty.xml