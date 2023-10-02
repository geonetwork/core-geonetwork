# Doing a GeoNetwork release {#doing-a-release}

This section documents the steps followed by the development team to do a new release.

Once the release branch has been thoroughly tested and is stable a release can be made.

1.  Prepare the release

    ``` shell
    # Setup properties
    frombranch=origin/main
    versionbranch=4.2.x
    version=4.2.3
    minorversion=0
    newversion=$version-$minorversion
    currentversion=4.2.3-SNAPSHOT
    previousversion=4.2.2
    nextversion=4.2.4-SNAPSHOT
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
    cat <<EOF > docs/changes$newversion.txt
    ================================================================================
    ===
    === GeoNetwork $version: List of changes
    ===
    ================================================================================
    EOF
    git log --pretty='format:- %s' $previousversion... >> docs/changes$newversion.txt
    ```

2.  Commit & tag the new version

    ``` shell
    # Then commit the new version
    git add .
    git commit -m "Update version to $newversion"

    # Create the release tag
    git tag -a $version -m "Tag for $version release"
    ```

3.  Build

    ``` shell
    # Build the new release
    mvn clean install -DskipTests -Pwar -Pwro4j-prebuild-cache


    # Download Jetty and create the installer
    cd release
    mvn clean install -Djetty-download
    ant


    # Deploy to osgeo repository (requires credentials in ~/.m2/settings.xml)
    mvn deploy
    ```

4.  Test

    ``` shell
    cd target/GeoNetwork-$newversion
    unzip geonetwork-bundle-$newversion.zip -d geonetwork-bundle-$newversion
    cd geonetwork-bundle-$newversion/bin
    ./startup.sh -f
    ```

5.  Set the next version

    ``` shell
    # Set version number to SNAPSHOT
    ./update-version.sh $newversion $nextversion

    # Add SQL migration step for the next version
    mkdir web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v424
    cat <<EOF > web/src/main/webapp/WEB-INF/classes/setup/sql/migrate/v424/migrate-default.sql
    UPDATE Settings SET value='4.2.4' WHERE name='system/platform/version';
    UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
    EOF
    vi web/src/main/webResources/WEB-INF/config-db/database_migration.xml
    ```

    In `WEB-INF/config-db/database_migration.xml` add an entry for the new version in the 2 steps:

    ``` xml
    <entry key="3.12.2">
      <list>
        <value>WEB-INF/classes/setup/sql/migrate/v3122/migrate-</value>
      </list>
    </entry>
    ```

    ``` shell
    git add .
    git commit -m "Update version to $nextversion"
    ```

6.  Publishing

    ``` shell
    # Push the branch and tag
    git push origin $versionbranch
    git push origin $version
    ```

7.  Generate checksum files

    -   If using Linux:

        ``` shell
        cd web/target && md5sum geonetwork.war > geonetwork.war.md5 && cd ../..
        cd release/target/GeoNetwork-$version && md5sum geonetwork-bundle-$newversion.zip >  geonetwork-bundle-$newversion.zip.md5 && cd ../../..
        ```

    -   If using Mac OS X:

        ``` shell
        md5 -r web/target/geonetwork.war > web/target/geonetwork.war.md5
        md5 -r release/target/GeoNetwork-$newversion/geonetwork-bundle-$newversion.zip > release/target/GeoNetwork-$newversion/geonetwork-bundle-$newversion.zip.md5
        ```

    On sourceforge first:

    ``` shell
    sftp $sourceforge_username,geonetwork@frs.sourceforge.net
    # For stable release
    cd /home/frs/project/g/ge/geonetwork/GeoNetwork_opensource
    # or for RC release
    cd /home/frs/project/g/ge/geonetwork/GeoNetwork_unstable_development_versions/
    mkdir v3.12.1
    cd v3.12.1
    put docs/changes3.12.1-0.txt
    put release/target/GeoNetwork*/geonetwork-bundle*.zip*
    put web/target/geonetwork.war*
    bye
    ```

8.  Update or add the changelog in the documentation <https://github.com/geonetwork/doc> .

9.  Close the milestone on github <https://github.com/geonetwork/core-geonetwork/milestones?state=closed> with link to sourceforge download.

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

    In the following folder `web/src/main/webapp/WEB-INF/classes/setup/sql/migrate` create `v370` folder.

    In this folder create a `migrate-default.sql` with the following content:

    ``` sql
    UPDATE Settings SET value='3.7.0' WHERE name='system/platform/version';
    UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
    ```

    In `web/src/main/webResources/WEB-INF/config-db/database_migration.xml` add the following for the migration to call the migration script:

    ``` xml
    <entry key="3.7.0">
      <list>
        <value>WEB-INF/classes/setup/sql/migrate/v370/migrate-</value>
      </list>
    </entry>
    ```

    Commit the new version

    ``` shell
    git add .
    git commit -m "Update version to $nextMajorVersion"
    git push origin master
    ```
