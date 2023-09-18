# Build from source code {#tuto-introduction-deployment-build}

In this paragraph some guidance will be provided to build GeoNetwork from source code. This is relevant in case you want to extend the application to your needs.

The source code of GeoNetwork is available at [Github](https://github.com/geonetwork/core-geonetwork). This means that you can clone, fork and propose pushes of your custom changes. If you are not familiar with repositories of code or git, you should check [this quick manual](https://try.github.io/levels/1/challenges/1).

GeoNetwork is a java project using [Maven version 3+](https://Maven.apache.org/). It is written on **Java 8**. It works both with OpenJDK or the Oracle version. There are several ways to install this on your local machine; for example if you have a Debian based OS (like Ubuntu), you can install them with just this command:

     $ sudo apt-get install maven git

Make sure you installed maven version 3!!

     $ mvn --version
       Apache Maven 3.2.1 (ea8b2b07643dbb1b84b6d16e1f08391b666bc1e9; 2014-02-14T18:37:52+01:00)
       Maven home: ....

Remember that this will also install Java on your system. You can check that the version is the right one with the following command:

     $ java -version

So, the very first step once you have your environment set up is clone the GeoNetwork repository on your local machine. That can be done on the command line using the following command inside an empty folder where the source code will be populated:

     $ cd yourEmptyFolder
     $ git clone https://github.com/geonetwork/core-geonetwork.git
     $ cd core-geonetwork
     $ git submodule init
     $ git submodule update

As you can see, all the source code shown on github is also available on your local machine now.

The source code of GeoNetwork is split on several smaller Maven projects. To run GeoNetwork, you have to build all of them and run the project named **"web"**.

If you are familiar to Maven, you will probably have guessed that you have to run a package install command on the root folder of GeoNetwork source code. But if you try that, Maven will warn you that for building GeoNetwork you need more memory than the default memory provided to Maven. This means, you will have to export the Maven options to increase the memory like this:

     $ export MAVEN_OPTS="-Xmx512M -XX:MaxPermSize=256M"

At this point we are not interested in running the tests, so you can skip them using the parameter *-DskipTests*:

     $ mvn package install -DskipTests

At the end of this build (which can take long, depending on your network connection, as it has many third party libraries), you will see something like this:

     [INFO] ------------------------------------------------------------------------

This will generate a war file, which you can use in any Java Application Container (server) like Tomcat, on web/target/geonetwork.war

Congratulations! You are ready to run GeoNetwork. To do this, just go to the web folder and run Jetty in there:

     $ cd web; mvn jetty:run

After Jetty starts, you can see your running GeoNetwork by opening a browser and enter to http://localhost:8080/geonetwork
