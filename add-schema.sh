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
  projectGroupId='${project.groupId}'
  gnSchemasVersion='${gn.schemas.version}'
  basedir='${basedir}'

  echo "Adding schema ${schema} dependency to web/pom.xml for schemaCopy"

  line=$(grep -n 'schema-iso19115-3.2018</artifactId>' web/pom.xml | cut -d: -f1 | tail -1)
  insertLine=$(($line + 2))
  sed $sedopt -f /dev/stdin web/pom.xml << SED_SCRIPT
  ${insertLine} a\\
\        <dependency>\\
\          <groupId>${projectGroupId}</groupId>\\
\          <artifactId>schema-${schema}</artifactId>\\
\          <version>${gnSchemasVersion}</version>\\
\        </dependency>
SED_SCRIPT

  echo "Adding schema ${schema} dependency to web/pom.xml for schemaUnpack"

  line=$(grep -n '</profiles>' web/pom.xml | cut -d: -f1)
  insertLine=$(($line - 1))
  sed $sedopt -f /dev/stdin web/pom.xml << SED_SCRIPT
  ${insertLine} a\\
\\
\    <profile>\\
\      <id>schema-${schema}</id>\\
\      <activation>\\
\        <property><name>schemasCopy</name><value>!true</value></property>\\
\        <file><exists>../schemas/${schema}</exists></file>\\
\      </activation>\\
\      <dependencies>\\
\        <dependency>\\
\          <groupId>${projectGroupId}</groupId>\\
\          <artifactId>schema-${schema}</artifactId>\\
\          <version>${gnSchemasVersion}</version>\\
\        </dependency>\\
\      </dependencies>\\
\      <build>\\
\        <plugins>\\
\          <plugin>\\
\            <groupId>org.apache.maven.plugins</groupId>\\
\            <artifactId>maven-dependency-plugin</artifactId>\\
\            <executions>\\
\              <execution>\\
\                <id>${schema}-resources</id>\\
\                <phase>process-resources</phase>\\
\                <goals><goal>unpack</goal></goals>\\
\                <configuration>\\
\                  <artifactItems>\\
\                    <artifactItem>\\
\                      <groupId>${projectGroupId}</groupId>\\
\                      <artifactId>schema-${schema}</artifactId>\\
\                      <type>jar</type>\\
\                      <overWrite>false</overWrite>\\
\                      <outputDirectory>${basedir}/src/main/webapp/WEB-INF/data/config/schema_plugins</outputDirectory>\\
\                      <includes>plugin/**</includes>\\
\                      <fileMappers>\\
\                        <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.RegExpFileMapper">\\
\                          <pattern>^plugin/</pattern><replacement>./</replacement>\\
\                        </fileMapper>\\
\                      </fileMappers>\\
\                    </artifactItem>\\
\                  </artifactItems>\\
\                </configuration>\\
\              </execution>\\
\            </executions>\\
\          </plugin>\\
\        </plugins>\\
\      </build>\\
\    </profile>
SED_SCRIPT
fi


# Add schema resources in web/pom.xml
line=$(grep -n "schemas/${schema}/src/main/plugin</directory>" web/pom.xml | cut -d: -f1)

if [ ! $line ]
then
  profileLine=$(grep -n '<id>schemas-copy</id>' web/pom.xml | cut -d: -f1)
  lineOffset=$(tail -n +$profileLine web/pom.xml | grep -n '<resources>' | cut -d: -f1 | head -1)
  finalLine=$(($profileLine + $lineOffset -1))

  projectBaseDir='${project.basedir}'
  baseDir='${basedir}'

  echo "Adding schema ${schema} resources to web/pom.xml for schemaCopy"

  sed $sedopt -f /dev/stdin web/pom.xml << SED_SCRIPT
  ${finalLine} a\\
\                    <resource>\\
\                      <directory>${projectBaseDir}/../schemas/${schema}/src/main/plugin</directory>\\
\                      <targetPath>${baseDir}/src/main/webapp/WEB-INF/data/config/schema_plugins</targetPath>\\
\                    </resource>
SED_SCRIPT
fi
