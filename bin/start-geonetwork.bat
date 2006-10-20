cd ..\jetty
rm log\*.log
java -Xmx512m -DSTOP.PORT=8079 -jar start.jar ..\bin\jetty.xml > log\console.log
