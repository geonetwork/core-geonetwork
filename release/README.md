Release
=======

The release module for GeoNetwork requires jetty to be downloaded before it will package an installer for use.

1. Build geonetwork if you have not done so already
   
   ```
   mvn clean install -DskipTests
   ```

2. From the release folder, download jetty (you only need to do this once):

   ```
   mvn process-resources -Djetty-download
   ```

3. Once the `jetty` folder is in place the release module will package the `zip` installer:

   ```
   mvn package
   ```
    
4. The installer is created in `target` folder.

3. To clean up the `jetty` folder:
   
   ```
   mvn clean:clean@reset
   ```
