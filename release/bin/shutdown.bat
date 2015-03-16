cd ..\jetty

set JETTY_HOME=.

java -DSTOP.PORT=8079 -DSTOP.KEY=geonetwork -jar start.jar --stop
