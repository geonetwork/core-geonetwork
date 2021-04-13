# Building

See [Installing from source code](https://geonetwork-opensource.org/manuals/trunk/en/maintainer-guide/installing/installing-from-source-code.html) (Maintainer Guide)

Build GeoNetwork
----------------

Once you checked out the code from Github repository, go inside the GeoNetwork’s root folder and execute the maven build command:

```
mvn clean install
```

If the build is successful you'll get an output like:
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

Compilation options
-------------------

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

Please refer to the maven documentation for additional options, [Maven: The Complete Reference](http://www.sonatype.com/books/mvnref-book/reference/public-book.html)

Maven Profiles
--------------

Maven profiles are used to enable additional build configuration.

Some components (eg. WFS feature indexing) of the application rely on an Elasticsearch instance. To enable those options, build the application with the ```es``` profile.

```
mvn clean install -Pes
```

Run embedded Jetty server
-------------------------

Maven comes with built-in support for Jetty via a [jetty-maven-plugin](https://www.eclipse.org/jetty/documentation/current/jetty-maven-plugin.html).

To run GeoNetwork with the embedded Jetty server you have to change directory to the root of the **web** module, and then execute the following maven command:

```
cd web
mvn jetty:run -Penv-dev
```

After startup, GeoNetwork is accessible at: http://localhost:8080/geonetwork

For changes related to the user interface in the `web-ui` module or the metadata schemas in the `schemas` module, can be deployed in jetty executing the following maven command in the **web** module:

```
mvn process-resources
```

For additional information see web [readme](../web/README.md).

Tool chain
----------

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
