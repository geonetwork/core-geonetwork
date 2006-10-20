#!/bin/bash

java -cp lib/csw-client.jar:lib/csw-common.jar:../web/WEB-INF/lib/jdom.jar:../web/WEB-INF/lib/dlib.jar \
			org.fao.geonet.csw.client.CswClient
