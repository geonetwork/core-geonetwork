Release
=======

The release module for GeoNetwork.

Open a terminal window and execute the following steps from within the ``release`` folder.


* Once GeoNetwork has been built (run Maven in the repository root), download Jetty:

    `
    mvn clean install -Djetty-download
    `

* Next, create the ZIP distributions and copy the WAR:

    `
    ant
    `

