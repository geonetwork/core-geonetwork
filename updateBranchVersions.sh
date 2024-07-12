#!/bin/bash

# Usage to update branch from a release 2.6.2 version to 2.6.3-SNAPSHOT version
# In root folder of branch code: ./updateBranchVersion.sh 2.6.2 2.6.3

function showUsage
{
  echo -e "\nThis script is used to update branch from a release version to next SNAPSHOT version. Should be used in branch after creating a new release (tag)."
  echo
  echo -e "Usage: ./`basename $0 $1` actual_version next_version"
  echo
  echo -e "Example to update file versions from 2.7.0 to 2.7.1-SNAPSHOT:"
  echo -e "\t./`basename $0 $1` 2.7.0 2.7.1"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

if [ $# -ne 2 ]
then
  showUsage
  exit
fi

if [[ $1 =~ ^[0-9]+.[0-9]+.[0-9]+$ ]]; then
    echo
else
	echo
	echo 'Update failed due to incorrect versionnumber format (' $1 ')'
	echo 'The format should be three numbers separated by dots. e.g.: 2.7.0'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.0 2.7.1"
	echo
	exit
fi

if [[ $2 =~ ^[0-9]+.[0-9]+.[0-9]+$ ]]; then
    echo
else
	echo
	echo 'Update failed due to incorrect new versionnumber format (' $2 ')'
	echo 'The format should be three numbers separated by dots. e.g.: 2.7.1'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.0 2.7.1"
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
new_version="$2"

# Update release properties
sed $sedopt "s/version=${version}/version=${new_version}/g" release/build.properties
sed $sedopt "s/subVersion=0/subVersion=SNAPSHOT/g" release/build.properties

# Update version pom files
find . -name pom.xml -exec sed $sedopt "s/${version}/${new_version}-SNAPSHOT/g" {} \;
