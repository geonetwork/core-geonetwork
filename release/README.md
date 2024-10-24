Release
=======

The release module for GeoNetwork.

Open a terminal window and execute the following steps from within the ``release`` folder.

1. Once GeoNetwork has been built (run Maven in the repository root), download Jetty:

   ```bash
   mvn clean install -Pjetty-download
   ```
   
   This will download the version of jetty indicated in dependency management, and rename to ``jetty`` folder
   (adjusting ``jetty-deploy.xml`` configuration to use `web` rather than default ``webapps``).

2. Next, create the ZIP distributions and copy the WAR:

   ```bash
   ant
   ```
   
   The build.xml file will check everything is available and assemble into a zip.

## Jetty download

To clean up the ``jetty`` download, when switching between branches:

```bash
mvn clean:clean@reset
```

