#!/bin/bash

buildRequiredApps=( "java" "git" "mvn" "ant" "xmlstarlet" )

for app in "${buildRequiredApps[@]}"; do :
   if ! [ -x "$(command -v ${app})" ]; then
     echo "Error: ${app} is not installed." >&2
     exit 1
   fi
done

function showUsage
{
  echo -e "\nThis script is used to build a release for the current branch"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

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

echo "Creating change log and release notes for version ${newversion} (from ${currentversion}). Git branch ${gitBranch}:"
echo "  docs/changes/changes$newversion.txt"
echo "  docs/manual/docs/overview/change-log/version-$mainVersion.md"
echo "When generated please review and update:"
echo "  docs/manual/mkdocs.yml"
echo "  docs/manual/docs/overview/latest/index.md"
echo "  docs/manual/docs/overview/change-log/version-$mainVersion.md"
echo ""
read -p "Press enter to continue"

# Generate list of changes
cat <<EOF > docs/changes/changes$newversion.txt
================================================================================
===
=== GeoNetwork $version: List of changes
===
================================================================================
EOF
git log --pretty='format:- %s' $previousversion... >> docs/changes/changes$newversion.txt

# Generate release notes

cat <<EOF > docs/manual/docs/overview/change-log/version-$mainVersion.md
# Version $mainVersion

GeoNetwork $mainVersion is a minor release.

## Migration notes

### API changes

### Installation changes

### Index changes

## List of changes

Major changes:

EOF

git log --pretty='format:* %N' $previousversion.. | grep -v "^* $" >> docs/manual/docs/overview/change-log/version-$mainVersion.md

cat <<EOF >> docs/manual/docs/overview/change-log/version-$mainVersion.md

and more \... see [$newversion issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A$mainVersion+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A$mainVersion+is%3Aclosed) for full details.
EOF
