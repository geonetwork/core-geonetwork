#!/bin/bash

projectVersion=`xmlstarlet sel -t -m "/_:project/_:version" -v . -n pom.xml`
version=`cut -d "-" -f 1 <<< $projectVersion`
versionbranch=`git branch --show-current`

echo "Testing zip in release/target/GeoNetwork-$version ..."

cd "release/target/GeoNetwork-$version"
unzip -q "geonetwork-bundle-$projectVersion.zip" -d "geonetwork-bundle-$projectVersion"
cd "geonetwork-bundle-$projectVersion/bin"
./startup.sh -f
