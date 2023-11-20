# Building from Source Code {#installing-from-source-code}

## System Requirements

### Java 11

GeoNetwork 4.4 is a Java 11 application that runs as a servlet, which means that a Java Development Kit (JDK) must be installed in order to build and run it. You can get a Java 11 JDK from your Linux distribution, [OpenJDK](https://openjdk.java.net/) or [Adoptium Temurin JDK](https://adoptopenjdk.net).

Please note that the commercial distribution of [Oracle JDK](https://www.oracle.com/technetwork/java/javase/downloads) no longer provides Java 11 for testing purposes.

Because GeoNetwork is developed with Java 11 (LTS), it  won't run at all with earlier releases.

-  GeoNetwork should not be developed with newer versions of Java.
-  It  won't run at all with earlier releases.
-  Java 17 (LTS) is **not supported** at this time.

### Application Server

Next, you need a servlet container. GeoNetwork ships with an embedded container, [Eclipse Jetty](https://www.eclipse.org/jetty/), which is fast and well-suited for most applications.

We highly recommend [Apache Tomcat](https://tomcat.apache.org). Apache Tomcat provides load balancing, fault tolerance and other production features. Apache Tomcat is widely used with many organizations as a standardized environment for all their Java Web Applications.

We recommend the following stable releases of Tomcat:

-   Apache Tomcat 9.0
-   Apache Tomcat 8.5

GeoNetwork cannot use the newer versions of Apache Tomcat 10 which are based on the Jakarata Enterprise Edition web application standard.

### Database

Regarding storage, you need a Database Management System (DBMS) like Oracle, MySQL, Postgresql etc. GeoNetwork comes with an embedded DBMS (H2) which is used by default during installation. This DBMS can be used for small or desktop installations of no more than a few thousand metadata records with one or two users. If you have heavier demands then you should use a professional, stand alone DBMS.

### Environment

Being written in Java, GeoNetwork can run on any platform that supports Java: primarily Linux, Windows and macOS.

GeoNetwork is not resource intensive and will not require a powerful machine. Good performance can be obtained with 1GB of RAM. However, the suggested amount is 2GB of RAM.

For hard disk space, you have to consider the space required for the application itself (about 350 MB) and the space required for data, which could grow up to 50 GB or more. A simple (SSD) disk of 250 GB should be sufficient in most cases. You also need some disk space for the search index which is located in `GEONETWORK_DATA_DIR/index` (by default GEONETWORK_DATA_DIR is `INSTALL_DIR/web/geonetwork/WEB_INF/data`). However, even with a few thousand metadata records, the index is small so usually 500 MB of space is more than enough.

The software runs in different ways depending on the servlet container you are using:

-   *Tomcat* - GeoNetwork is available as a WAR file which you can put into the Tomcat webapps directory. Tomcat will deploy the WAR file when it is started. You can then use the Tomcat manager web application to stop/start GeoNetwork. You can also use the startup.* and shutdown.* scripts located in the Tomcat bin directory (.* means .sh or .bat depending on your OS) but if you have other web applications in the Tomcat container, then they will also be affected.
-   *Jetty* - If you use the provided container you can use the scripts in GeoNetwork's bin directory. The scripts are startup.* and shutdown.* and you must be inside the bin directory to run them. You can use these scripts just after installation.

## Tools

The following tools are required to be installed to setup a development environment for GeoNetwork:

-   **Java 11** - Developing with GeoNetwork requires Java Development Kit (JDK) 11.
-   **Maven** 3.1.0+ - GeoNetwork uses [Maven](https://maven.apache.org/) to manage the build process and the dependencies. Once is installed, you should have the mvn command in your path (on Windows systems, you have to open a shell to check).
-   **Git** - GeoNetwork source code is stored and versioned in [a Git repository on Github](https://github.com/geonetwork/core-geonetwork). Depending on your operating system a variety of Git clients are available. Please check the Git website for some [alternatives](https://git-scm.com/downloads/guis) and good [documentation](https://git-scm.com/documentation). More documentation can be found on the [Github website](https://help.github.com/).
-   **Ant** - GeoNetwork uses [Ant](https://ant.apache.org/) to build the installer. Version 1.6.5 works but any other recent version should be OK. Once installed, you should have the Ant command in your path (on Windows systems, you have to open a shell to check).
-   **mkdocs** - To build the GeoNetwork documentation in a nice format, [mkdocs](https://www.mkdocs.org) is used. Please note that if you don't have a Python interpreter on your system, Sphinx will not work, so you need to install [Python](https://www.python.org/downloads/).

## Building & Running

If you only wish to quickly build and run GeoNetwork, execute the following:

``` shell
git clone --depth 3 --recursive https://github.com/geonetwork/core-geonetwork.git
cd core-geonetwork
mvn clean install -DskipTests

cd es
mvn install -Pes-download
mvn exec:exec -Des-start

cd web
mvn jetty:run
```

Now open your browser and navigate to <http://localhost:8080/geonetwork>.

For a more detailed explanation, please read on.

### Check out the source code

Clone the repository and build:

``` shell
git clone --recursive https://github.com/geonetwork/core-geonetwork.git
cd core-geonetwork
mvn clean install -DskipTests
```

### Submodules

GeoNetwork use submodules, these were initialized by the `--recursive` option when cloning the repository.

If you missed using `--recursive` run the following:

``` shell
cd core-geonetwork
git submodule init
git submodule update
```

Submodules are used to keep track of externals dependencies. It is necessary to init and update them after a branch change:

``` shell
git submodule update --init
```

Remember to rebuild the application after updating external dependencies.

### Build GeoNetwork

Once you checked out the code from Github repository, go inside the GeoNetwork's root folder and execute the Maven build command:

``` shell
mvn clean install
```

If the build is successful you'll get an output like:

``` shell
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

Your local Maven repository now contain the GeoNetwork artifacts created (`$HOME/.m2/repository/org/geonetwork-opensource`).

### Compilation options

Many Maven build options are available, for instance, you might like to use following options :

``` shell
-- Skip test
$ mvn install -DskipTests

-- Offline use
$ mvn install -o

-- Build really fast with 2 threads per cpu core
$ mvn install -o -DskipTests -T 2C
```

Please refer to the [Maven documentation](https://www.sonatype.com/books/mvnref-book/reference/public-book.html) for any other options.

### Run embedded Jetty server

Maven comes with built-in support for Jetty via a [plug-in](https://eclipse.dev/jetty/documentation/jetty-9/index.html#maven-and-jetty).

To run GeoNetwork with the embedded Jetty server you have to change directory to the root of the **web** module, and then execute the following Maven command:

``` shell
mvn jetty:run -Penv-dev
```

After some moments of startup and initialization, GeoNetwork is available at: <http://localhost:8080/geonetwork>

For changes related to the user interface in the ``web-ui`` module or the metadata schemas in the `schemeas` module, these can be deployed in Jetty executing the following Maven command in the **web** module:

``` shell
mvn process-resources -DschemasCopy=true
```
