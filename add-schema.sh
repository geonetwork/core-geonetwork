#!/bin/bash

function showUsage
{
  echo -e "\nThis script is used to add a metadata schema in GeoNetwork for development"
  echo
  echo -e "Usage: ./`basename $0 $1` schema_name git_schema_repository git_schema_branch"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0 $1` iso19139.ca.HNAP https://github.com/metadata101/iso19139.ca.HNAP 3.10.x"
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
  git submodule add --force -b ${gitBranch} ${gitRepository} schemas/${schema}
fi


# Add schema module in schemas/pom.xml
line=$(grep -n ${schema} schemas/pom.xml | cut -d: -f1)

if [ ! -n "$line" ]
then
  line=$(grep -n '</profiles>' schemas/pom.xml | cut -d: -f1)
  insertLine=$(($line - 1))

  echo "Adding schema ${schema} to schemas/pom.xml"

  sed $sedopt -f /dev/stdin schemas/pom.xml << SED_SCRIPT
  ${insertLine} a\\
\    <profile>\\
\      <id>schema-${schema}</id>\\
\      <activation>\\
\        <file><exists>${schema}</exists></file>\\
\      </activation>\\
\      <modules>\\
\        <module>${schema}</module>\\
\      </modules>\\
\    </profile>
SED_SCRIPT
fi

# Add schema dependency in web/pom.xml
line=$(grep -n "schema-${schema}" web/pom.xml | cut -d: -f1)

if [ ! -n "$line" ]
then
  line=$(grep -n 'schema-iso19139</artifactId>' web/pom.xml | cut -d: -f1)
  insertLine=$(($line + 2))

  projectGroupId='org.geonetwork-opensource.schemas'
  gnSchemasVersion='${project.version}'

  echo "Adding schema ${schema} dependency to web/pom.xml"

  sed $sedopt -f /dev/stdin web/pom.xml << SED_SCRIPT
  ${insertLine} a\\
\    <dependency>\\
\      <groupId>${projectGroupId}</groupId>\\
\      <artifactId>schema-${schema}</artifactId>\\
\      <version>${gnSchemasVersion}</version>\\
\    </dependency>
SED_SCRIPT
fi

# Add schema resources in service/pom.xml with test scope for unit tests
line=$(grep -n "<artifactId>schema-${schema}</artifactId>" services/pom.xml | cut -d: -f1)

if [ ! $line ]
then
  line=$(grep -n '</dependencies>' services/pom.xml | cut -d: -f1)
  finalLine=$(($line - 1))

  projectGroupId='org.geonetwork-opensource.schemas'
  gnSchemasVersion='${project.version}'

  echo "Adding schema ${schema} resources to service/pom.xml"

  sed $sedopt -f /dev/stdin services/pom.xml << SED_SCRIPT
  ${finalLine} a\\
\    <dependency>\\
\      <groupId>${projectGroupId}</groupId>\\
\      <artifactId>schema-${schema}</artifactId>\\
\      <version>${gnSchemasVersion}</version>\\
\      <scope>test</scope>\\
\    </dependency>
SED_SCRIPT
fi
