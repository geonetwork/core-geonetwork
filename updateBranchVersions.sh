#!/bin/bash

# Usage to update branch from a release 2.6.2 version to 2.6.3-SNAPSHOT version 		
# In root folder of branch code: ./updateBranchVersion.sh 2.6.2 2.6.3

version="$1"
new_version="$2"

# Update version in sphinx doc files
sed -i .bak  "s/${version}/${new_version}-SNAPSHOT/g" docs/eng/users/source/conf.py 
sed -i .bak  "s/${version}/${new_version}-SNAPSHOT/g" docs/eng/developer/source/conf.py

# Update version pom files
find . -name pom.xml -exec sed -i .bak "s/${version}/${new_version}-SNAPSHOT/g" {} \;