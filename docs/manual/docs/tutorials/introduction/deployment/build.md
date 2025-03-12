# Build from source code {#tuto-introduction-deployment-build}

In this paragraph some guidance will be provided to build GeoNetwork from source code. This is relevant in case you want to extend the application to your needs.

The source code of GeoNetwork is available at [Github](https://github.com/geonetwork/core-geonetwork). This means that you can clone, fork and propose pushes of your custom changes. If you are not familiar with repositories of code or git, you should check [this quick manual](https://try.github.io/levels/1/challenges/1).

GeoNetwork 4.4 is a java project using [Maven version 3+](https://Maven.apache.org/). It is written on **Java 11**. It works both with OpenJDK or the Oracle version. There are several ways to install this on your local machine; for example if you have a Debian based OS (like Ubuntu), you can install them with just this command:

``` bash
$ sudo apt-get install maven git
```

Make sure you installed maven version 3!!

``` bash
$ mvn --version
Apache Maven 3.2.1 (ea8b2b07643dbb1b84b6d16e1f08391b666bc1e9; 2014-02-14T18:37:52+01:00)
Maven home: ....
```

Remember that this will also install Java on your system. You can check that the version is the right one with the following command:

``` bash
$ java -version
```

So, the very first step once you have your environment set up is clone the GeoNetwork repository on your local machine. That can be done on the command line using the following command inside an empty folder where the source code will be populated:

``` bash
$ cd yourEmptyFolder
$ git clone https://github.com/geonetwork/core-geonetwork.git
$ cd core-geonetwork
$ git submodule init
$ git submodule update
```

As you can see, all the source code shown on github is also available on your local machine now.

The source code of GeoNetwork is split on several smaller Maven projects. To run GeoNetwork, you have to build all of them and run the project named **"web"**.

If you are familiar to Maven, you will probably have guessed that you have to run a package install command on the root folder of GeoNetwork source code. But if you try that, Maven will warn you that for building GeoNetwork you need more memory than the default memory provided to Maven. This means, you will have to export the Maven options to increase the memory like this:

``` bash
$ export MAVEN_OPTS="-Xmx512M -XX:MaxPermSize=256M"
```

At this point we are not interested in running the tests, so you can skip them using the parameter *-DskipTests*:

``` bash
$ mvn package install -DskipTests
```

At the end of this build (which can take long, depending on your network connection, as it has many third party libraries), you will see something like this:

``` bash
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] GeoNetwork opensource ............................. SUCCESS [ 3.111 s]
[INFO] common utils ...................................... SUCCESS [ 13.678 s]
[INFO] Caching xslt module ............................... SUCCESS [ 7.607 s]
[INFO] ArcSDE module (dummy-api) ......................... SUCCESS [ 7.860 s]
[INFO] GeoNetwork domain ................................. SUCCESS [ 33.785 s]
[INFO] Oaipmh modules .................................... SUCCESS [ 0.833 s]
[INFO] GeoNetwork Events ................................. SUCCESS [ 0.654 s]
[INFO] GeoNetwork schema plugins ......................... SUCCESS [ 4.646 s]
[INFO] GeoNetwork schema plugins core .................... SUCCESS [ 5.338 s]
[INFO] GeoNetwork schema plugin for ISO19139/119 standards SUCCESS [ 8.432 s]
[INFO] GeoNetwork core ................................... SUCCESS [ 16.304 s]
[INFO] GeoNetwork schema plugin for Dublin Core records retrieved by CSW SUCCESS [ 5.031 s]
[INFO] GeoNetwork schema plugin for Dublin Core standard . SUCCESS [ 8.419 s]
[INFO] GeoNetwork schema plugin for ISO19110 standard .... SUCCESS [ 3.627 s]
[INFO] GeoNetwork CSW server ............................. SUCCESS [ 5.546 s]
[INFO] GeoNetwork harvesters ............................. SUCCESS [ 3.888 s]
[INFO] GeoNetwork health monitor ......................... SUCCESS [ 2.489 s]
[INFO] GeoNetwork services ............................... SUCCESS [ 8.597 s]
[INFO] Geonetwork Web Resources 4 Java ................... SUCCESS [ 5.261 s]
[INFO] Cobweb Customizations ............................. SUCCESS [ 4.226 s]
[INFO] GeoNetwork INSPIRE Atom ........................... SUCCESS [ 3.990 s]
[INFO] Tests for schema plugins .......................... SUCCESS [ 2.334 s]
[INFO] GeoNetwork user interface module .................. SUCCESS [ 35.356 s]
[INFO] JS API and Service documentation .................. SUCCESS [ 21.203 s]
[INFO] GeoNetwork web client module ...................... SUCCESS [ 47.484 s]
[INFO] GeoNetwork Web module ............................. SUCCESS [ 48.490 s]
[INFO] GeoNetwork E2E Javascript Tests ................... SUCCESS [ 1.645 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 02:19 min (Wall Clock)
[INFO] Finished at: 2015-07-17T10:36:43+01:00
[INFO] Final Memory: 232M/441M
[INFO] ------------------------------------------------------------------------
```

This will generate a war file, which you can use in any Java Application Container (server) like Tomcat, on web/target/geonetwork.war

Congratulations! You are ready to run GeoNetwork. To do this, just go to the web folder and run Jetty in there:

``` bash
$ cd web; mvn jetty:run
```

After Jetty starts, you can see your running GeoNetwork by opening a browser and enter to http://localhost:8080/geonetwork
