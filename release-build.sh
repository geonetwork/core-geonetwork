#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to build a release"
  echo
  echo -e "Usage: ./`basename $0 $1 $2` actual_version next_version previous_version"
  echo
  echo -e "Example to build release 4.4.0 with next version 4.4.1 and previous version 4.2.5:"
  echo -e "\t./`basename $0 $1` 4.4.0 4.4.1 4.2.5"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

buildRequiredApps=( "java" "git" "mvn" "ant" "xmlstarlet" )

for app in "${buildRequiredApps[@]}"; do :
   if ! [ -x "$(command -v ${app})" ]; then
     echo "Error: ${app} is not installed." >&2
     exit 1
   fi
done


if [ $# -ne 3 ]
then
  showUsage
  exit
fi

# Setup properties
from=origin
frombranch=origin/main
series=4.4
#versionbranch=$series.x
versionbranch=main
version=$1
minorversion=0
release=latest
newversion=$version-$minorversion
currentversion=$1-SNAPSHOT
previousversion=$3
nextversion=$2-SNAPSHOT
nextMajorVersion=4.6.0-SNAPSHOT



echo "Creating release for version $newversion (from $currentversion). Next version will be $nextversion"

git clone --recursive https://github.com/geonetwork/core-geonetwork.git \
          geonetwork-$versionbranch
cd geonetwork-$versionbranch

if [ $versionbranch -ne "main" ]
then
  git checkout -b $versionbranch $frombranch
fi



# TODO: Transifex update
# TODO: Changelog

# Update version number (in pom.xml, installer config and SQL)
./update-version.sh $currentversion $newversion
# TODO: Check the JRE URL replacement

# Generate list of changes
cat <<EOF > docs/changes/changes$newversion.txt
================================================================================
===
=== GeoNetwork $version: List of changes
===
================================================================================
EOF
git log --pretty='format:- %s' $previousversion... >> docs/changes/changes$newversion.txt

# Then commit the new version
git add .
git commit -m "Update version to $newversion"
git tag -a $version -m "Tag for $version release"

# Build the new release
mvn clean install -DskipTests -Pwar -Pwro4j-prebuild-cache

(cd datastorages && mvn clean install -Drelease -DskipTests)


# Download Jetty and create the installer
(cd release && mvn clean install -Pjetty-download && ant)

# Set version number to SNAPSHOT
./update-version.sh $newversion $nextversion

git add .
git commit -m "Update version to $nextversion"


rm release/target/GeoNetwork-$version/geonetwork-bundle-$newversion.zip.MD5
if [[ ${OSTYPE:0:6} == 'darwin' ]]; then
  md5 -r web/target/geonetwork.war > web/target/geonetwork.war.md5
  md5 -r release/target/GeoNetwork-$newversion/geonetwork-bundle-$newversion.zip > release/target/GeoNetwork-$newversion/geonetwork-bundle-$newversion.zip.md5
else
  (cd web/target && md5sum geonetwork.war > geonetwork.war.md5)
  (cd release/target/GeoNetwork-$version && md5sum geonetwork-bundle-$newversion.zip > geonetwork-bundle-$newversion.zip.md5)
fi
