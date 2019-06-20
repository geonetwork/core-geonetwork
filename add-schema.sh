#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to add a metadata schema in GeoNetwork for development"
  echo
  echo -e "Usage: ./`basename $0 $1` schema_name git_schema_repository git_schema_branch"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0 $1` iso19115-3 https://github.com/metadata101/iso19115-3 3.4.x"
  echo
}

if [ "$1" = "-h" ]
then
	showUsage
	exit
fi

if [ $# -ne 3 ]
then
  showUsage
  exit
fi

schema=$1
gitRepository=$2
gitBranch=$3

# Note: In MacOS (darwin10.0) sed requires -i .bak as option to work properly
if [[ ${OSTYPE:0:6} == 'darwin' ]]; then
	sedopt='-i .bak'
else
	sedopt='-i'
fi


# Add submodule
if [ ! -d "schemas/${schema}" ]; then
  echo "Adding schema from ${gitRepository}, branch  ${gitBranch} to schemas/${schema}"
  git submodule add -b ${gitBranch} ${gitRepository} schemas/${schema}
fi


# Add schema module in schemas/pom.xml
line=$(grep -n ${schema} schemas/pom.xml | cut -d: -f1)

if [ ! $line ]
then
  line=$(grep -n 'iso19139</module>' schemas/pom.xml | cut -d: -f1)

  echo "Adding schema ${schema} to schemas/pom.xml"

  sed $sedopt "${line} a\\
    <module>${schema}</module>
  " schemas/pom.xml
fi


# Add schema dependency in web/pom.xml
line=$(grep -n "schema-${schema}" web/pom.xml | cut -d: -f1)

if [ ! $line ]
then
  line=$(grep -n 'schema-iso19139</artifactId>' web/pom.xml | cut -d: -f1)
  insertLine=$(($line + 2))

  projectGroupId='${project.groupId}'
  gnSchemasVersion='${gn.schemas.version}'

  echo "Adding schema ${schema} dependency to web/pom.xml"

  sed $sedopt "${insertLine} a\\
<dependency>\\
<groupId>${projectGroupId}</groupId>\\
<artifactId>schema-${schema}</artifactId>\\
<version>${gnSchemasVersion}</version>\\
</dependency>
  " web/pom.xml
fi


# Add schema resources in web/pom.xml
line=$(grep -n "schemas/${schema}/src/main/plugin</directory>" web/pom.xml | cut -d: -f1)

if [ ! $line ]
then
  line=$(grep -n 'schemas/iso19139/src/main/plugin</directory>' web/pom.xml | cut -d: -f1)
  finalLine=$(($line + 3))

  projectBaseDir='${project.basedir}'
  baseDir='${basedir}'

  echo "Adding schema ${schema} resources to web/pom.xml"

  sed $sedopt "${finalLine} a\\
 <resource>\\
    <directory>${projectBaseDir}/../schemas/${schema}/src/main/plugin</directory>\\
    <targetPath>${baseDir}/src/main/webapp/WEB-INF/data/config/schema_plugins</targetPath>\\
  </resource>
  " web/pom.xml
fi
