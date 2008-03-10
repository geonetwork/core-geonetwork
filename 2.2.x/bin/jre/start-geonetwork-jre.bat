cd ..\jetty
del log\*request.log*
move log\jeeves.log.* log\archive

..\jre1.5.0_12\bin\java -Xms48m -Xmx512m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml