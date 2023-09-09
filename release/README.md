Release
=======

The release module for GeoNetwork.

To generate ``zip`` bundles use the release profile (``release``, ``cmis``, ``jcloud``, ``s3``):
```
mvn clean install -Drelease
```

To generate only for the ``release`` module:
```bash
cd release
mvn clean install -Drelease
```

## Manual 

Open a terminal window and execute the following steps from within the ``release`` folder.

* Once GeoNetwork has been built (run Maven in the repository root), download Jetty:

    `
    mvn clean install -Pjetty-download
    `

* Next, create the ZIP distributions and copy the WAR:

    `
    ant
    `

