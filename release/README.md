RELEASE
=======

The release module for Geonetwork

* Build release and download Jetty

`
cd ..
mvn clean install -Djetty-download
`

* Create ZIP distributions

`
cd release
ant
`

