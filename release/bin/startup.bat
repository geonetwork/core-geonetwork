
set JETTY_HOME=../jetty
set JETTY_BASE=../jetty
cd %JETTY_HOME%

del logs\*request.log*
del logs\output.log
move logs\geonetwork.log.* logs\archive

java -Dmime-mappings=..\web\geonetwork\WEB-INF\mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar

rem Try changing the Xmx parameter if having memory errors
rem java -Xms1g -Xmx1g -Xss2M -Dmime-mappings=..\web\geonetwork\WEB-INF\mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar

rem If you want to hide the dos window when GeoNetwork is started, comment the previous line and comment out the last line
rem start javaw -Dmime-mappings=..\web\geonetwork\WEB-INF\mime-types.properties -DSTOP.PORT=8079 -Djava.awt.headless=true -DSTOP.KEY=geonetwork -jar start.jar
