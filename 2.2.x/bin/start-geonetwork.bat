cd ..\jetty
del log\*request.log*
move log\geonetwork.log.* log\archive
move log\intermap.log.*   log\archive

rem try changing the Xmx parameter if your machine has little RAM
rem java -Xms48m -Xmx256m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml

rem if you want to hide the dos window when GeoNetwork is started, 
rem comment out the next line and comment the last line

java -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml

rem start javaw -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml