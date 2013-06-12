.. _stable:

Create a release
================

This guide details the process of performing a release.

.. note:: 
    
    * BRANCH: Branches are created for major stables releases and end with .x (for example 2.8.x) 
    * VERSION (for tag): version to release (for example 2.8.1 or 2.8.1RC0) 

Call for vote
-------------

In order to make a release ask the PSC to vote for the new release.


Release committee
-----------------

To create new releases a committee of 2-4 persons should be chosen. The members of the committee are 
responsible for creating the releases following the steps described in this section.

A rotation policy can be use to select a person from the committee who will be responsible 
for creating each release. 

Notify developer lists
----------------------

It is good practice to notify the GeoNetwork developer list of the intention to make the release 
a few days in advance.

On the day the release is being made a warning mail **must** be sent to the list asking that developers 
refrain from committing until the release tag has been created.

Prerequisites
-------------

#. Commit access to `GeoNetwork git <https://github.com/geonetwork>`_
#. Administration rights to SourceForge server to publish the release
#. Administration rights to geonetwork-opensource.org to update the website (for stable release)

Add release to trac
-------------------

Add the new version to the trac release list (http://trac.osgeo.org/geonetwork/admin/ticket/versions).

Add the 3 RC milestone (http://trac.osgeo.org/geonetwork/admin/ticket/milestones).

::

  TODO: At some point, all open tickets for the master branch (eg. 2.9.0) should be moved
  to the next RC (eg. 2.10.0RC1 or 2.10.1) or the next master (eg. 2.11.0).
  
  


Making the release
------------------

This procedure creates a new development branch and its first RC version.

TODO : Add procedure to only make a new release from an existing dev branch.

::
  
  # Set version numbers
  modules=( "docs" "gast" "geoserver" "installer" )
  # TODO maybe add other modules if changes
  version=2.10.0
  devversion=2.10.x
  minorversion=RC0
  masterversion=2.9.0
  previousversion=2.8.x
  sourceforge_username=YourSourceforgeUserName
  
  # Get the code
  git clone --recursive https://github.com/geonetwork/core-geonetwork.git geonetwork-$version
  cd geonetwork-$version
  
  
  
  # Create a new development branch from master
  git checkout -b $devversion origin/master
  git submodule foreach git checkout -b $devversion origin/master
  
  # Update version number
  ./update-version.sh $masterversion $version-SNAPSHOT
  
  # Commit the new x branch (if it does not exist) for project and modules
  for i in "${modules[@]}"
  do
        cd $i; git add .; git commit -m "Update version to $version-SNAPSHOT"; cd ..
  done
  git add .
  git commit -m "Update version to $version-SNAPSHOT"
  
  
  
  # Create the new release
  ./update-version.sh $version $version-$minorversion
  
  # Commit the new minor version for modules
  for i in "${modules[@]}"
  do
        cd $i; git add .; git commit -m "Update version to $version-$minorversion"; cd ..
  done
  git add .
  
  
  # Compile
  mvn clean install -Pwith-doc
  
  # Build the application and run the integration tests in ``web-itests``
  cd web-itests
  mvn clean install -Pitests
  cd ..
  
  
  # Build installer
  cd installer
  ant
  cd ..
  
  # Test the installer
  
  # Generate list of changes
  cat <<EOF > docs/changes$devversion.txt
  ================================================================================
  ===
  === GeoNetwork $version: List of changes
  ===
  ================================================================================
  EOF
  git log --pretty='format:- %s' origin/$previousversion... >> docs/changes$devversion.txt
  
  
  # Tag the release
  git tag -a $version$minorversion -m "Tag for $version-$minorversion release"
  git push origin $version$minorversion
  
  
  
  
  # Restore version number to SNAPSHOT
  ./update-version.sh $version $version-SNAPSHOT
  for i in "${modules[@]}"
  do
        cd $i; git add .; git commit -m "Update version to $version-SNAPSHOT"; cd ..
  done
  git add .
  git commit -m "Update version to $version-SNAPSHOT"
  
  
  
  # Push to github - could be done at the end of the process ?
  # git submodule foreach `git push origin $devversion`
  for i in "${modules[@]}"
  do
        cd $i; git add .; git push origin $devversion; cd ..
  done
  git push origin $devversion
  
  
  # Publish in sourceforge
  sftp $sourceforge_username,geonetwork@frs.sourceforge.net
  # For stable release
  cd /home/frs/project/g/ge/geonetwork/GeoNetwork_opensource
  # or for RC release
  cd /home/frs/project/g/ge/geonetwork/cd GeoNetwork_unstable_development_versions/
  mkdir 2.10.0
  cd 2.10.0
  put docs/changes*.txt
  put geonetwork*/*.jar
  put web/target/geonetwork.war
  
  
  # Publish on the website
  # TODO



Updating the master branch version number
-----------------------------------------

After a new development branch is created, it is required to update master version number.
The following procedure could be applied::
  
  masterversion=2.9.0
  version=2.11.0
  modules=( "docs" "gast" "geoserver" "installer" )
  # Get the code
  git clone --recursive https://github.com/geonetwork/core-geonetwork.git geonetwork-$version
  cd geonetwork-$version
  
  # Update version
  ./update-version.sh $masterversion $version-SNAPSHOT
  
  # Update some SQL (TODO)
  git checkout -- web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v${masterversion//[.]/}/migrate-default.sql
  mkdir web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v${version//[.]/}
  
  # Commit
  # git submodule foreach git add .
  # FIXME : don't work due to $version ?
  #git submodule foreach git commit -m "Update version to $version-SNAPSHOT."
  for i in "${modules[@]}"
  do
        cd $i; git add .; git commit -m "Update version to $version-SNAPSHOT"; cd ..
  done
  
  git add .
  git commit -m "Update version to $version-SNAPSHOT."
  git submodule foreach git push origin master
  git push origin master
  

Upload and release on SourceForge
---------------------------------

All of the artifacts generated so far need to be uploaded to the SourceForce File release System:

1. WAR distribution
2. Installers (exe and jar)

.. note:: This step requires administrative privileges in SourceForge for the GeoNetwork opensource project.

1. Log in to `SourceForge <http://sourceforge.net/account/login.php>`_.

2. Go to the ` GeoNetwork Files section <https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/>`_.

3. Add the new v[VERSION] folder for this release.

4.a. Using the commandline secure copy is the simplest way for developers working under a \*NIX like system: ::

    $ scp geonetwork.war username@frs.sourceforge.net:/home/frs/project/g/ge/geonetwork/GeoNetwork_opensource/v[VERSION]/
    $ scp geonetwork-[VERSION].jar username@frs.sourceforge.net:/home/frs/project/g/ge/geonetwork/GeoNetwork_opensource/v[VERSION]/
    $ scp geonetwork-[VERSION].exe username@frs.sourceforge.net:/home/frs/project/g/ge/geonetwork/GeoNetwork_opensource/v[VERSION]/
    $ scp docs/readme[VERSION].txt username@frs.sourceforge.net:/home/frs/project/g/ge/geonetwork/GeoNetwork_opensource/v[VERSION]/

4.b. The same can be accomplished in Windows using `WinSCP <http://winscp.net/>`_. Or a desktop client like `Cyberduck <http://cyberduck.ch/>`_ on Windows and Mac OS X

5. Once the upload of the files has been completed, use the web interface to set the default download files. 
The (i) button allows to set the default operating systems for each installer (.exe for Windows and .jar for all other systems).

.. image:: filerelease.png
    :align: right
    :alt: Details of the Windows installer file 

6. The default downloads are ready now.

Update geonetwork-opensource website
------------------------------------

The website requires updates to reflect the new release. Update the version number and add a new news entry in the following files::

  website/docsrc/conf.py
  website/docsrc/docs.rst
  website/docsrc/downloads.rst
  website/docsrc/index.rst
  website/docsrc/news.rst
  website/checkup_docs.sh 
  
Commit the changes and build the website using the `Hudson deployment system <http://thor.geocat.net/hudson/>`_

Announce the release
--------------------

Mailing lists
`````````````
Send an email to both the developers list and users list announcing the release.

Template email for RC::
    
    Release Candidate X v[VERSION] now available for testing
    
    Dear all,
    You can now download and test the release candidate (v[VERSION]RCX) of GeoNetwork opensource version [VERSION]:
    
    https://sourceforge.net/projects/geonetwork/files/GeoNetwork_unstable_development_versions/v[VERSION]/
    
    We did not generate a dedicated Windows installer, but this multiplatform installer should also work on Windows systems.
    
    You can find a list of new functionality in v[VERSION] at http://trac.osgeo.org/geonetwork/wiki/proposals
     as well as in the documentation included in the installer.
    
    A list of fixes and changes in [VERSION] here https://github.com/geonetwork/core-doc-sources/blob/master/changes[VERSION].txt.
    
    If you have any fixed or improvements you want to contribute back, the best is to use git to get a local copy of the source code, apply the fix and put out a Pull request so your improvements can be integrated quickly. Otherwise you can also create new Tickets in the http://trac.osgeo.org/geonetwork issue tracker.
    
    Looking forward to your testing, feedback and contributions. The release of  GeoNetwork opensource v2.8.0 stable release is planned in about two weeks. 
    
    Thanks to all developers and contributors!
    Happy testing,
    
    

Template mail for release::

  TODO