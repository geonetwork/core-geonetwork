# Doing a GeoNetwork release {#doing-a-release}

This section documents the steps followed by the development team to do a new release.

Once the release branch has been thoroughly tested and is stable a release can be made.

The following script can be used on Linux and Mac. For this a running build environment is needed
with the following utilities: ***sed***, ***xmlstarlet*** and ***sftp***.


1.  Prepare the release (examples prepairs version 4.2.1 as latest release):

    ``` shell
    # Setup properties
    from=origin
    frombranch=origin/4.2.x
    series=4.2
    versionbranch=$series.x
    version=4.2.1
    minorversion=0
    release=latest
    newversion=$version-$minorversion
    currentversion=4.2.1-SNAPSHOT
    previousversion=4.2.0
    nextversion=4.2.2-SNAPSHOT
    nextMajorVersion=4.4.0-SNAPSHOT

    # Get the branch
    git clone --recursive https://github.com/geonetwork/core-geonetwork.git \
              geonetwork-$versionbranch
    cd geonetwork-$versionbranch


    # Create or move to the branch for the version
    # Create it if it does not exist yet
    git checkout -b $versionbranch $frombranch
    # or move into it if it exist
    # git checkout $versionbranch
    # or stay in main branch if the release is on main


    # Update version number (in pom.xml, installer config and SQL)
    ./update-version.sh $currentversion $newversion

    # Generate list of changes
    cat <<EOF > docs/changes/changes$newversion.txt
    ================================================================================
    ===
    === GeoNetwork $version: List of changes
    ===
    ================================================================================
    EOF
    git log --pretty='format:- %s' $previousversion... >> docs/changes/changes$newversion.txt
    ```

2.  Prepare change-log notes.

    Git notes are managed similar to push and pulling tags. Start by pulling the latest notes:
    ```
    git pull origin refs/notes/commits 
    ```
    
    Review changes along with any notes:
    ```
    git log --pretty='format:%h: %s %n      note: %N' $previousversion...
    ```
    
    Use `git note append` to document commits adding major features.
    
    ```
    git notes append <sha> -m "<description of major feature>"
    ```
    
    Use `git note remove` if you need to clear a note and start again:
    ```
    git notes remove <sha>
    ```
    
    Preview changes using:
    
    ```
     git log --pretty='format:* %N' $previousversion... | grep -v "^* $"
    ```
    
    Save your notes:
    ```
    git push origin refs/notes/commits
    ```

3.  Create change log page: `docs/manual/docs/overview/change-log/`

    ``` shell
    cat <<EOF > docs/manual/docs/overview/change-log/version-$version.md
    # Version $version
    
    GeoNetwork $version is a minor release.

    ## Migration notes
    
    ### API changes
    
    ### Installation changes
    
    ### Index changes
    
    ## List of changes

    Major changes:
    
    EOF

    git log --pretty='format:* %N' $previousversion... | grep -v "^* $" >> docs/manual/docs/overview/change-log/version-$version.md

    cat <<EOF > docs/manual/docs/overview/change-log/version-$version.md
    
    and more \... see [$version issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A$version+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A$version+is%3Aclosed) for full details.
    EOF
    ```
    
    Fill in the above markdown file, removing any unused headings.

4. Update links and navigation:
    
    * ``docs/manual/mkdocs.yml``
    * ``docs/manual/docs/overview/change-log/index.md``
    * ``docs/manual/docs/overview/change-log/latest.md``
    * ``docs/manual/docs/overview/change-log/stable.md``
    * ``docs/manual/docs/overview/change-log/archive.md``
    
    Test documentation locally:
    ```
    cd docs/manual
    mkdocs serve
    ```
    Once running check the new page:
    ```
    open http://localhost:8000/ocverview/change-log/$newversion
    ```

5.  Commit & tag the new version

    ``` shell
    # Then commit the new version
    git add .
    git commit -m "Update version to $newversion"

    # Create the release tag
    git tag -a $version -m "Tag for $version release"
    ```

6.  Build

    ``` shell
    # deep clean
    mvn clean:clean@reset
    
    # Build the new release
    mvn install -Drelease
    
    # Create a minimal war
    cd web
    mvn clean install -DskipTests -Pwar -Pwro4j-prebuild-cache

    # Download Jetty and create the installer
    cd ../release
    mvn clean install -Pjetty-download,bundle

    # Deploy to osgeo repository (requires credentials in ~/.m2/settings.xml)
    cd ..
    mvn deploy -Drelease
    ```

7.  Test

    ``` shell
    cd target/GeoNetwork-$version
    unzip geonetwork-bundle-$newversion.zip -d geonetwork-bundle-$newversion
    cd geonetwork-bundle-$newversion/bin
    ./startup.sh -f
    ```

8.  Set the next version

    ``` shell
    # Set version number to SNAPSHOT
    ./update-version.sh $newversion $nextversion

    nextversionnosnapshot=${nextversion//[-SNAPSHOT]/}
    
    # Add SQL migration step for the next version
    mkdir web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v${nextversionnosnapshot//[.]/}
    cat <<EOF > web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v${nextversionnosnapshot//[.]/}/migrate-default.sql
    UPDATE Settings SET value='${nextversionnosnapshot}' WHERE name='system/platform/version';
    UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
    EOF
    vi web/src/main/webResources/WEB-INF/config-db/database_migration.xml
    ```

    In `WEB-INF/config-db/database_migration.xml` add an entry for the new version in the 2 steps:

    ``` xml
    <entry key="4.2.2">
      <list>
        <value>WEB-INF/classes/setup/sql/migrate/v422/migrate-</value>
      </list>
    </entry>
    ```

    ``` shell
    git add .
    git commit -m "Update version to $nextversion"
    ```

9.  Publishing

    ``` shell
    # Push the branch and tag
    git push origin $versionbranch
    git push origin $version
    ```

10.  Generate checksum files

    -   If using Linux:

        ``` shell
        cd web/target && md5sum geonetwork.war > geonetwork.war.md5 && cd ../..
        cd release/target/GeoNetwork-$version && md5sum geonetwork-bundle-$newversion.zip >  geonetwork-bundle-$newversion.zip.md5 && cd ../../..
        ```

    -   If using Mac OS X:

        ``` shell
        md5 -r web/target/geonetwork.war > web/target/geonetwork.war.md5
        md5 -r release/target/GeoNetwork-$version/geonetwork-bundle-$newversion.zip > release/target/GeoNetwork-$version/geonetwork-bundle-$newversion.zip.md5
        ```

    On sourceforge first:

    ``` shell
    sftp $sourceforge_username,geonetwork@frs.sourceforge.net
    # For stable release
    cd /home/frs/project/g/ge/geonetwork/GeoNetwork_opensource
    # or for RC release
    cd /home/frs/project/g/ge/geonetwork/GeoNetwork_unstable_development_versions/
    mkdir v4.2.1
    cd v4.2.1
    put docs/changes/changes4.2.1-0.txt
    put release/target/GeoNetwork*/geonetwork-bundle*.zip*
    put web/target/geonetwork.war*
    bye
    ```

1.  Close the milestone on github <https://github.com/geonetwork/core-geonetwork/milestones?state=closed> with link to sourceforge download.

    Publish the release on github <https://github.com/geonetwork/core-geonetwork/releases> .

    Update the website links <https://github.com/geonetwork/website> .

    -   Add the changes file for the release to <https://github.com/geonetwork/doc/tree/develop/source/overview/change-log>
    -   List the previous file in <https://github.com/geonetwork/doc/blob/develop/source/overview/change-log/index.rst>
    -   Update the version: <https://github.com/geonetwork/website/blob/master/docsrc/conf.py>
    -   Update the download link: <https://github.com/geonetwork/website/blob/master/docsrc/downloads.rst>
    -   Add the section for the new release: <https://github.com/geonetwork/website/blob/master/docsrc/news.rst>

    Send an email to the mailing lists.

10. Merge in depending branches

    If a major version, then master version has to be updated to the next one (eg. if 3.8.0, then 3.7.x is 3.9.x).

    ``` shell
    # Create it if it does not exist yet
    git checkout master
    ./update-version.sh $currentversion $nextMajorVersion
    ```
    
    Update documentation to reflect series change of `latest`, `stable`, `maintenance` and `archive`:
    
    * ``docs/manual/mkdocs.yml`` navigation changes as branches change role
    * ``docs/manual/docs/overview/change-log/index.md``
    * ``docs/manual/docs/overview/change-log/latest.md``
    * ``docs/manual/docs/overview/change-log/stable.md``
    * ``docs/manual/docs/overview/change-log/archive.md``

    Commit the new version

    ``` shell
    git add .
    git commit -m "Update version to $nextMajorVersion"
    git push origin master
    ```
