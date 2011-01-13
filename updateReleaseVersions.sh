#!/bin/bash

# Usage to create 2.6.2 release version from 2.6.2-SNAPSHOT
# In root folder of branch code: ./updateReleaseVersion.sh 2.6.2

# Note: In MacOs change seed -i to seed -i .bak to work properly

function showUsage
{
  echo -e "\nThis script is used to update branch from a SNAPSHOT version to a release version. Should be used in branch before creating a new release (tag)." 
  echo -e "\nUsage:"
  echo -e "\t`basename $0 $1` {version}"
  echo -e "\nExample to update file versions from 2.6.2-SNAPSHOT to 2.6.2:"
  echo -e "\t`basename $0 $1` 2.6.2"
  echo -e "\n"
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

version="$1"

# Update version in sphinx doc files
sed -i "s/${version}-SNAPSHOT/${version}/g" docs/eng/users/source/conf.py 
sed -i "s/${version}-SNAPSHOT/${version}/g" docs/eng/developer/source/conf.py

# Update installer
sed -i "s/\<property name=\"subVersion\" value=\"SNAPSHOT\" \/\>/\<property name=\"subVersion\" value=\"0\" \/\>/g" installer/build.xml

# Update version pom files
find . -name pom.xml -exec sed -i "s/${version}-SNAPSHOT/${version}/g" {} \;