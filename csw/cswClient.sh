#!/bin/bash

CP=lib/csw-client.jar:lib/csw-common.jar

for i in ../web/WEB-INF/lib/*.jar ; do
	CP=${CP}:$i
done

java -cp $CP org.fao.geonet.csw.client.CswClient
