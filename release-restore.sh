#!/bin/bash

buildRequiredApps=( "java" "git" "mvn" "ant" "xmlstarlet" )

for app in "${buildRequiredApps[@]}"; do :
   if ! [ -x "$(command -v ${app})" ]; then
     echo "Error: ${app} is not installed." >&2
     exit 1
   fi
done

projectVersion=`xmlstarlet sel -t -m "/_:project/_:version" -v . -n pom.xml`
subVersion=`cut -d "-" -f 2 <<< $projectVersion`
mainVersion=`cut -d "-" -f 1 <<< $projectVersion`
mainVersionMajor=`cut -d "." -f 1 <<< $mainVersion`
mainVersionMinor=`cut -d "." -f 2 <<< $mainVersion`
mainVersionSub=`cut -d "." -f 3 <<< $mainVersion`

gitBranch=`git branch --show-current`

nextVersionNumber="${mainVersionMajor}.${mainVersionMinor}.$((mainVersionSub+1))"
previousVersionNumber="${mainVersionMajor}.${mainVersionMinor}.$((mainVersionSub-1))"

from=origin
frombranch=origin/${gitBranch}
series=${mainVersionMajor}.${mainVersionMinor}
versionbranch=${gitBranch}
version=${projectVersion}
minorversion=0
release=latest
newversion=${mainVersion}-$minorversion
currentversion=${projectVersion}
previousversion=${previousVersionNumber}
nextversion=${nextVersionNumber}-SNAPSHOT


echo "Update version number to ${nextversion} (from ${newversion})."
echo ""
echo "After update. Push changes to Git branch ${gitBranch}."
read -p "Press enter to continue"


# Set version number to SNAPSHOT
./update-version.sh $newversion $nextversion

git add .
git commit -m "Update version to $nextversion"

