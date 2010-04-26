Geoserver deploy
----------------

Execute ant to deploy geoserver in ../web/geoserver folder. The task is executed automatically when the installer is created


Files
-----

All included files are official versions downloaded from http://geoserver.org/:

1) geoserver-2.0.1.war: GeoServer application
2) geoserver-2.0.1-charts-plugin.zip: Charts plugin
3) geoserver-2.0.1-restconfig-plugin-zip: REST API plugin
4) styler.zip: Styler application. Accessible in http://localhost:8080/geoserver/styler/

5) web.xml: Same web.xml included in war file, modified to customize the data folder
