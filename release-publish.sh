#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to publish a release on sourceforge, github and maven repository"
  echo
  echo -e "Usage: ./`basename` sourceforge_username"
  echo
  echo -e "Example:"
  echo -e "\t./`basename ` sourceforgeusername"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

if [ $# -ne 1 ]
then
  showUsage
  exit
fi

projectVersion=`xmlstarlet sel -t -m "/_:project/_:version" -v . -n pom.xml`
version=`cut -d "-" -f 1 <<< $projectVersion`
versionbranch=`git branch --show-current`
sourceforge_username=$1

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
