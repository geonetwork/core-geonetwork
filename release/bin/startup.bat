
set JETTY_HOME=../jetty
set JETTY_BASE=../jetty
cd %JETTY_HOME%

del logs\*request.log*
del logs\output.log
move logs\geonetwork.log.* logs\archive

java -Xms512m -Xmx1g --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED -Djetty.httpConfig.requestHeaderSize=32768 -Dorg.eclipse.jetty.server.Request.maxFormContentSize=500000 -Dorg.eclipse.jetty.server.Request.maxFormKeys=4000 -Dmime-mappings=..\web\geonetwork\WEB-INF\mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar

rem Try changing the Xmx parameter if having memory errors
rem If you want to hide the dos window when GeoNetwork is started, comment the previous line and comment out the last line
rem start javaw -Dmime-mappings=..\web\geonetwork\WEB-INF\mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar
