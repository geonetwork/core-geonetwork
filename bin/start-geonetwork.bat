cd ..\jetty
del log\*request.log*
move log\geonetwork.log.* log\archive
move log\intermap.log.*   log\archive

rem try changing the Xmx parameter if your machine has little RAM
rem java -Xms48m -Xmx256m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml

java -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml