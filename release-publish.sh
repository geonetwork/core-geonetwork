#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to publish a release on sourceforge, github and maven repository"
  echo
  echo -e "Usage: ./`basename $0` sourceforge_username [remote]"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0` sourceforgeusername"
  echo -e "\t./`basename $0` sourceforgeusername upstream"
  echo
}

if [ "$1" = "-h" ]
then
  showUsage
  exit
fi

if [[ ($# -ne 1) && ($# -ne 2) ]]
then
  showUsage
  exit
fi

projectVersion=`xmlstarlet sel -t -m "/_:project/_:version" -v . -n pom.xml`
version=`cut -d "-" -f 1 <<< $projectVersion`
versionbranch=`git branch --show-current`
sourceforge_username=$1
remote=origin

if [ $# -eq 2 ]
then
  remote=$2
fi

# Push the branch and tag to github
git push $remote $versionbranch
git push $remote $version
# TODO: attach release notes to version

sftp $sourceforge_username,geonetwork@frs.sourceforge.net << EOT
cd /home/frs/project/g/ge/geonetwork/GeoNetwork_opensource
mkdir v${version}
cd v${version}
put docs/changes/changes${version}-0.txt
put release/target/GeoNetwork*/geonetwork-bundle*.zip*
put web/target/geonetwork.war*
put datastorages/*/target/*.zip
put plugins/*/target/*.zip
bye
EOT

# Deploy to osgeo repository (requires credentials in ~/.m2/settings.xml)
mvn deploy -DskipTests -Drelease
