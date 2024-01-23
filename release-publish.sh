#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to publish a release on sourceforge, github and maven repository"
  echo
  echo -e "Usage: ./`basename $0 $1` branch version sourceforge_username"
  echo
  echo -e "Example to publish 4.4.0:"
  echo -e "\t./`basename $0 $1` main 4.4.0 sourceforgeusername"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

if [ $# -ne 3 ]
then
  showUsage
  exit
fi

versionbranch=$1
version=$2
sourceforge_username=$3

sourceforge_username=XXXXX

sftp $sourceforge_username,geonetwork@frs.sourceforge.net << EOT
cd /home/frs/project/g/ge/geonetwork/GeoNetwork_opensource
mkdir v${version}
cd v${version}
put docs/changes/changes{$version}-0.txt
put release/target/GeoNetwork*/geonetwork-bundle*.zip*
put web/target/geonetwork.war*
put datastorages/*/target/*.zip
bye
EOT


# Push the branch and tag
git push origin $versionbranch
git push origin $version

# Deploy to osgeo repository (requires credentials in ~/.m2/settings.xml)
mvn deploy -Drelease
