#!/bin/bash

# Usage to create 2.6.2 release version from 2.6.2-SNAPSHOT
# In root folder of branch code: ./updateReleaseVersion.sh 2.6.2

function showUsage
{
  echo -e "\nThis script is used to update branch from a SNAPSHOT version to a release version. Should be used in branch before creating a new release (tag)."
  echo
  echo -e "Usage: `basename $0 $1` version"
  echo
  echo -e "Example to update file versions from 2.7.0-SNAPSHOT to 2.7.0:"
  echo -e "\t`basename $0 $1` 2.7.0"
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

# Update release subversion
sed $sedopt "s/subVersion=SNAPSHOT/subVersion=0/g" release/build.properties

# Update version pom files
find . -name pom.xml -exec sed $sedopt "s/${version}-SNAPSHOT/${version}/g" {} \;
