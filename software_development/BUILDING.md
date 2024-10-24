# Building

See [Installing from source code](https://docs.geonetwork-opensource.org/4.2/install-guide/installing-from-source-code/) (Installation Guide)

## Build GeoNetwork

Once you checked out the code from GitHub repository, go inside the GeoNetwork’s root folder and execute the maven build command:

```
mvn clean install
```

If the build is successful, you'll get an output like:
```
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] ------------------------------------------------------------------------
[INFO] GeoNetwork opensource ................................. SUCCESS [1.345s]
[INFO] Caching xslt module ................................... SUCCESS [1.126s]
[INFO] Jeeves modules ........................................ SUCCESS [3.970s]
[INFO] ArcSDE module (dummy-api) ............................. SUCCESS [0.566s]
[INFO] GeoNetwork web client module .......................... SUCCESS [23.084s]
[INFO] GeoNetwork user interface module ...................... SUCCESS [15.940s]
[INFO] Oaipmh modules ........................................ SUCCESS [1.029s]
[INFO] GeoNetwork domain ..................................... SUCCESS [0.808s]
[INFO] GeoNetwork core ....................................... SUCCESS [6.426s]
[INFO] GeoNetwork CSW server ................................. SUCCESS [2.050s]
[INFO] GeoNetwork health monitor ............................. SUCCESS [1.014s]
[INFO] GeoNetwork harvesters ................................. SUCCESS [2.583s]
[INFO] GeoNetwork services ................................... SUCCESS [3.178s]
[INFO] GeoNetwork Web module ................................. SUCCESS [2:31.387s]
[INFO] ------------------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3 minutes 35 seconds
[INFO] Finished at: Sun Oct 27 16:21:46 CET 2013
```

Your local maven repository now contain the GeoNetwork artifacts created (``$HOME/.m2/repository/org/geonetwork-opensource``).

## Compilation options

Many Maven build options are available, for instance, you might like to use following options :

* To skip tests:

  ```
  mvn install -DskipTests
  ```

* Offline use:

  ```
  mvn install -o
  ```

* Build really fast with 2 threads per cpu core

  ```
  mvn install -o -DskipTests -T 2C
  ```

Please refer to the maven documentation for additional options, [Maven: The Complete Reference](https://books.sonatype.com/mvnref-book/pdf/mvnref-pdf.pdf)

## Maven Profiles

Maven profiles are used to enable additional build configuration.

## Run Elasticsearch

GeoNetwork requires an Elasticsearch instance as an index.

1. To run, download using maven:

   ```
   cd es
   mvn install -Pes-download
   ```

2. And run locally:
   ```
   mvn exec:exec -Des-start
   ```

3. For alternatives see [es/readme](../es/README.md).

## Run Kibana

1. To run, download using maven:

   ```
   cd es/es-dashboard
   mvn install -Pkb-download
   ```
   
2. Run locally:

   ```
   mvn exec:exec -Dkb-start
   ```
   
3. For alternatives see [es/es-dashboards/readme](../es/es-dashboards/README.md).

## Run embedded Jetty server

Maven comes with built-in support for Jetty via a [jetty-maven-plugin](https://eclipse.dev/jetty/documentation/jetty-12/programming-guide/index.html#jetty-maven-plugin).

To run GeoNetwork with the embedded Jetty server:

1. Change directory to the root of the **web** module, and then execute the following maven command:

   ```
   cd web
   mvn jetty:run -Penv-dev
   ```

2. After a moment, GeoNetwork is available at: http://localhost:8080/geonetwork

3. The default `h2` database is located in your home folder `~/gn.mv.db` and `~/gn.trace.db` (based on system property ``db.name``).

4. For changes related to the user interface in the `web-ui` module or the metadata schemas in the `schemas` module, can be deployed in jetty executing the following maven command in the **web** module:

   ```
   mvn process-resources -PschemasCopy
   ```

5. To reset all caches and database use:
   
   ```
   cd web
   mvn clean:clean@reset
   ```

6. For more information see [web/README.md](../web/README.md).



To start the application under the root context, use:

   ```
   cd web
   mvn jetty:run -Dgeonetwork.webapp.contextpath=/
   ```


## Tool chain

GeoNetwork requires Java 8 at this time. If you have multiple JDK environments installed
our build can make use of an optional `~/.m2/toolchains.xml` file.

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
    </provides>
    <configuration>
    <jdkHome>/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

If the `toolchains.xml` file is available a profile will be engaged to ensure a JDK `8` is used. For more information see [guide to using toolchains](https://maven.apache.org/guides/mini/guide-using-toolchains.html).
