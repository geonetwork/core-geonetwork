cd ..\jetty
del logs\*request.log*
del logs\output.log
move logs\geonetwork.log.* logs\archive
move logs\intermap.log.*   logs\archive
move logs\geoserver.log.* logs\archive

rem try changing the Xmx parameter if your machine has little RAM
rem java -Xms48m -Xmx256m -XX:MaxPermSize=128m -DSTOP.PORT=8079 -DSTOP.KEY=geonetwork -jar start.jar ..\bin\jetty.xml

rem if you want to hide the dos window when GeoNetwork is started, 
rem comment out the next line and comment the last line

java -Xms48m -Xmx512m -XX:MaxPermSize=128m -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ..\bin\jetty.xml

rem start javaw -Xms48m -Xmx512m -XX:MaxPermSize=128m -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar ..\bin\jetty.xml