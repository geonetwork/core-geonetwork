#!/usr/bin/env bash
rm edmo_output.xml
rm data/*

java -jar EdmerpSimpleList.jar > edmerp_output.xml

java -jar ../../../web/target/geonetwork/WEB-INF/lib/saxon-9.1.0.8b-patch.jar -s:edmerp_output.xml -xsl:EDMERP2Geonetwork.xsl

