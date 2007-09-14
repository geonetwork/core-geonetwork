cd ..\jetty
del log\*request.log*
move log\jeeves.log.* log\archive

..\jre1.5.0_12\bin\java -Xmx512m -DSTOP.PORT=8079 -DSTOP.KEY=geonetwork -jar start.jar ..\bin\jetty.xml