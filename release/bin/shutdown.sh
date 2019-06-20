#!/usr/bin/env bash

cd ../jetty
export JETTY_HOME=.
java -DSTOP.PORT=8079 -DSTOP.KEY=geonetwork -jar start.jar --stop
