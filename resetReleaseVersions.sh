#!/bin/bash

# Usage to reset a version number from e.g. 2.7.1 to 2.7.0
# In root folder of branch code: ./resetReleaseVersions.sh 2.7.1 2.7.0

version="$1"
new_version="$2"

function showUsage 
{
  echo -e "\nThis script is used to reset a version number from e.g. 2.7.1 to 2.7.0" 
  echo
  echo -e "Usage: ./`basename $0 $1` actual_version next_version"
  echo
  echo -e "Example to update file versions from 2.7.0 to 2.7.1:"
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


if [[ $1 != [0-9].[0-9].[0-9] ]]; then 
	echo
	echo 'Update failed due to incorrect versionnumber format: ' $1
	echo 'The format should be three numbers separated by dots. e.g.: 2.7.0'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.1 2.7.0"
	echo
	exit
fi

if [[ $2 != [0-9].[0-9].[0-9] ]]; then 
	echo
	echo 'Update failed due to incorrect new versionnumber format (' $2 ')'
	echo 'The format should be three numbers separated by dots. e.g.: 2.7.1'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.1 2.7.0"
	echo
	exit
fi

# Note: In MacOS (darwin10.0) sed requires -i .bak as option to work properly
if [[ $OSTYPE == 'darwin10.0' ]]; then
	sedopt='-i .bak'
else
	sedopt='-i'
fi

echo
echo 'Your Operating System is' $OSTYPE 
echo 'sed will use the following option: ' $sedopt
echo

# Update version in sphinx doc files
sed $sedopt "s/${version}/${new_version}/g" docs/eng/users/source/conf.py 
sed $sedopt "s/${version}/${new_version}/g" docs/eng/developer/source/conf.py

# Update ZIP distribution
sed $sedopt "s/\<property name=\"version\" value=\"${version}\" \/\>/\<property name=\"version\" value=\"${new_version}\" \/\>/g" release/build.xml

# Update version pom files
find . -name pom.xml -exec sed $sedopt "s/${version}/${new_version}/g" {} \;
