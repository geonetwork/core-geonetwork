#!/bin/bash

# Usage to create 3.10.2 release version from 3.10-SNAPSHOT
# In root folder of branch code: ./updateReleaseVersion.sh 3.10.2

function showUsage
{
  echo -e "\nThis script is used to update branch from a SNAPSHOT version to a release version. Should be used in branch before creating a new release (tag)."
  echo
  echo -e "Usage: `basename $0 $1` version"
  echo
  echo -e "Example to update file versions from 2.7.0-SNAPSHOT to 3.10.2:"
  echo -e "\t`basename $0 $1` 3.10.2"
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

if [[ $1 =~ ^[0-9]+.[0-9]+.[0-9]+$ ]]; then
    echo
else
	echo 'Update failed due to incorrect versionnumber format: ' $1
	echo 'The format should be three numbers separated by dots. e.g.: 2.7.0'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.0"
	echo
	exit
fi

# Note: In MacOS (darwin10.0) sed requires -i .bak as option to work properly
if grep -q "darwin" <<< $OSTYPE ; then
	sedopt='-i .bak'
else
	sedopt='-i'
fi

echo
echo 'Your Operating System is' $OSTYPE
echo 'sed will use the following option: ' $sedopt
echo

version="$1"
# Remove the patch version: 3.10.2 --> 3.10
versionnopatchinfo="${version%.*}"

# Update version in sphinx doc files
sed $sedopt "s/${versionnopatchinfo}-SNAPSHOT/${version}/g" docs/manuals/source/conf.py

# Update release subversion
sed $sedopt "s/version=.*/version=${version}/g" release/build.properties
sed $sedopt "s/subVersion=SNAPSHOT/subVersion=0/g" release/build.properties

# Update version pom files
mvn versions:set-property -Dproperty=gn.project.version -DnewVersion=${version}
echo 'Module'
mvn versions:set -DnewVersion=${version} -DgenerateBackupPoms=false -Pwith-doc
echo
