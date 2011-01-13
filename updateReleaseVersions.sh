#!/bin/bash

# Usage to create 2.6.2 release version from 2.6.2-SNAPSHOT
# In root folder of branch code: ./updateReleaseVersion.sh 2.6.2

# Note: In MacOs change seed -i to seed -i .bak to work properly

version="$1"

# Update version in sphinx doc files
sed -i "s/${version}-SNAPSHOT/${version}/g" docs/eng/users/source/conf.py 
sed -i "s/${version}-SNAPSHOT/${version}/g" docs/eng/developer/source/conf.py

# Update installer
sed -i "s/\<property name=\"subVersion\" value=\"SNAPSHOT\" \/\>/\<property name=\"subVersion\" value=\"0\" \/\>/g" installer/build.xml

# Update version pom files
find . -name pom.xml -exec sed -i "s/${version}-SNAPSHOT/${version}/g" {} \;