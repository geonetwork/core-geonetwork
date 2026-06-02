#!/bin/bash

# Usage to reset a version number from e.g. 2.7.1 to 2.7.0
# In root folder of branch code: ./resetReleaseVersions.sh 2.7.1 2.7.0

# Source version eg. 2.6.0
version="$1"
# Target version eg. 2.7.0-RC0
new_version="$2"
# Target version main number eg. 2.7.0
new_version_main="0"
# Target version main number without separator eg. 270
new_version_main_nopoint="0"
# Target version minor version number eg. RC0
sub_version="0"

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


if [[ $1 =~ ^[0-9]+.[0-9]+.[0-9x]+(-SNAPSHOT|-RC[0-2]|-alpha.[0-9]+|-[0-9]+)?$ ]]; then
    echo
else
	echo
	echo 'Update failed due to incorrect versionnumber format: ' $1
	echo 'The format should be three numbers separated by dots with optional -SNAPSHOT. e.g.: 2.7.0 or 2.7.0-SNAPSHOT or 2.7.x-SNAPSHOT'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.0 2.7.0-RC0"
	echo
	exit
fi

if [[ $2 =~ ^[0-9]+.[0-9]+.[0-9x]+(-SNAPSHOT|-RC[0-2]|-alpha.[0-9]+|-[0-9]+)?$ ]]; then
    # Retrieve version and subversion
    if [[ $2 =~ ^[0-9]+.[0-9]+.[0-9]+-.*$ ]]; then
        new_version_main=`echo $2 | cut -d- -f1`
        sub_version=`echo $2 | cut -d- -f2`
    else
        new_version_main=$2
        sub_version="0"
    fi
    new_version_main_nopoint=${new_version_main//[.]/}
else
	echo
	echo 'Update failed due to incorrect new versionnumber format (' $2 ')'
	echo 'The format should be three numbers separated by dots with optional -SNAPSHOT. e.g.: 2.7.0 or 2.7.0-SNAPSHOT'
	echo
	echo "Usage: ./`basename $0 $1` 2.7.0 2.7.0-RC0"
	echo
	exit
fi


echo
echo 'Source version is: ' $version
echo 'Target version is: ' $new_version
echo 'Target main version is: ' $new_version_main
echo 'Target main version (without point) is: ' $new_version_main_nopoint
echo 'Target sub version is: ' $sub_version
echo


# Note: In MacOS (darwin10.0) sed requires -i .bak as option to work properly
if [[ ${OSTYPE:0:6} == 'darwin' ]]; then
	sedopt='-i .bak'
else
	sedopt='-i'
fi

echo
echo 'Your Operating System is: ' $OSTYPE
echo 'sed will use the following option: ' $sedopt
echo

# TODO: check that version is the version in the file to be updated.

# Update release properties
echo 'Release (ZIP bundle)'
echo '  * updating release/build.properties'
sed $sedopt "s/version=.*/version=${new_version_main}/g" release/build.properties
sed $sedopt "s/subVersion=.*/subVersion=${sub_version}/g" release/build.properties
echo

# Update SQL - needs improvements
echo 'SQL script'
echo '  * Set version in initial data'
sqlscriptfolder=web/src/main/webapp/WEB-INF/classes/setup/sql
sed $sedopt "s/'system\/platform\/version', '.*', 0/'system\/platform\/version', '${new_version_main}', 0/g" $sqlscriptfolder/data/data-db-default.sql
sed $sedopt "s/'system\/platform\/subVersion', '.*', 0/'system\/platform\/subVersion', '${sub_version}', 0/g" $sqlscriptfolder/data/data-db-default.sql



echo "  * Set version in migration script v${new_version_main_nopoint}/migrate-default.sql"

sqlmigrationfile=$sqlscriptfolder/migrate/v${new_version_main_nopoint}/migrate-default.sql

if [ -f "$sqlmigrationfile" ]; then
  echo "    * Updating version in existing migration script $sqlmigrationfile."
  sed $sedopt "s/value='${version}' WHERE name='system\/platform\/version'/value='${new_version_main}' WHERE name='system\/platform\/version'/g" $sqlmigrationfile
  sed $sedopt "s/value='.*' WHERE name='system\/platform\/subVersion'/value='${sub_version}' WHERE name='system\/platform\/subVersion'/g" $sqlmigrationfile
else
  echo "    * Creating migration script $sqlmigrationfile."
  mkdir $sqlscriptfolder/migrate/v${new_version_main_nopoint}
  cat <<EOF > $sqlmigrationfile
UPDATE Settings SET value='${new_version_main}' WHERE name='system/platform/version';
UPDATE Settings SET value='${sub_version}' WHERE name='system/platform/subVersion';
EOF
fi


sqlmigrationconfig=web/src/main/webResources/WEB-INF/config-db/database_migration.xml
if hash xmlstarlet 2>/dev/null; then
  xmlstarlet ed -L \
      -s "//*[@id='dataMigrationMap' and not(*[local-name() = 'entry' and @key ='${new_version_main}'])]" -t elem -n "newentry" \
      -i //newentry -t attr -n "key" -v "${new_version_main}" \
      -s //newentry -t elem -n "list" \
      -s //newentry/list -t elem -n "value" -v "WEB-INF/classes/setup/sql/migrate/v${new_version_main_nopoint}/migrate-" \
      -r //newentry -v entry \
      $sqlmigrationconfig
else
    echo "  * WARNING: Can't update automatically $sqlmigrationconfig. Install xmlstarlet utility or update file manually."
fi

# Update version pom files
mvn versions:set-property -Dproperty=gn.project.version -DnewVersion=${new_version}
echo 'Module'
mvn versions:set -DnewVersion=${new_version} -DgenerateBackupPoms=false -Pwith-doc
echo

