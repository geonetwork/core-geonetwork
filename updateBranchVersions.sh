#!/bin/bash

# Usage to update branch from a release 3.10.0 version to 3.10-SNAPSHOT version
# In root folder of branch code: ./updateBranchVersion.sh 3.10.0

function showUsage
{
  echo -e "\nThis script is used to update branch from a release version to next SNAPSHOT version. Should be used in branch after creating a new release (tag)."
  echo
  echo -e "Usage: ./`basename $0 $1` actual_version"
  echo
  echo -e "Example to update file versions from 3.10.0 to 3.10-SNAPSHOT:"
  echo -e "\t./`basename $0 $1` 3.10.0"
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
	echo
	echo 'Update failed due to incorrect versionnumber format (' $1 ')'
	echo 'The format should be three numbers separated by dots. e.g.: 3.10.0'
	echo
	echo "Usage: ./`basename $0 $1` 3.10.0"
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
# Remove patch number for SNAPSHOT version
new_version="${version%.*}"

# Update version in sphinx doc files
sed $sedopt "s/${version}/${new_version}-SNAPSHOT/g" docs/manuals/source/conf.py

# Update release properties
sed $sedopt "s/version=${version}/version=${new_version}/g" release/build.properties
sed $sedopt "s/subVersion=0/subVersion=SNAPSHOT/g" release/build.properties

# Update version pom files
mvn versions:set-property -Dproperty=gn.project.version -DnewVersion=${new_version}-SNAPSHOT
echo 'Module'
mvn versions:set -DnewVersion=${new_version}-SNAPSHOT -DgenerateBackupPoms=false -Pwith-doc
echo
