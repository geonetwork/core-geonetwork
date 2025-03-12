# Local deployment {#tuto-introduction-deployment-deploy}

Before deploying **GeoNetwork** you first need to set up an Elasticsearch instance. <https://www.elastic.co/downloads/elasticsearch> lists the various options for deployment, such as via the installer, a package manager such as yum, apt, brew or docker. Ensure you use a version of Elasticsearch compatible with the version of GeoNetwork. For GeoNetwork 4.x this is Elasticsearch 7.x.

GeoNetwork itself can be downloaded from [sourceforge](https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource). The zip distribution includes a jetty container ready to run GeoNetwork.

Verify that a java 1.8 run time (JRE/JDK) is available and active. Else download and install from <https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot>. Or activate via the \$JAVA_HOME environment variable.

``` bash
$ java --version
```

The war distribution can be deployed in a container such as Tomcat.

To install Tomcat on a debian-based operation system, you can use the apt-get tool like:

``` bash
$ sudo apt-get install tomcat7
```

For windows, download the installer from <https://tomcat.apache.org/download-80.cgi>.

Once you have Tomcat installed on your system, locate the webapps folder and place the geonetwork.war file there. This will deploy GeoNetwork on your system.

!!! note

    You need to ensure Tomcat is configured with enough memory for GeoNetwork to launch. This can be be configured via the `setenv` script in tomcat with the appropriate memory for the JAVA_OPTS property)


Open the file /geonetwork/WEB-INF/config.properties and alter the elasticsearch connection

``` bash
$ es.url=http://localhost:9200
```

Then (re)start Jetty/Tomcat.

You can make sure GeoNetwork is deployed via the following url: <http://localhost:8080/geonetwork>
