#!/usr/bin/env bash
rm edmo_output.xml
rm data/*

curl -o edmo_output.xml https://edmo.seadatanet.org/webservices/edmo/ws_edmo_get_list/

java -jar ../../../web/target/geonetwork/WEB-INF/lib/saxon-9.1.0.8b-patch.jar -s:edmo_output.xml -xsl:EDMO2Geonetwork.xsl

