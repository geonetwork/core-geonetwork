# GeoNetwork class loader for Tomcat

This class loader prioritizes local Apache `xerces` library from `WEB-INF/lib` folder over the version in
Tomcat `endorsed` folder, to avoid issues with `xml-resolver` library.

Other solution, that doesn't require a custom class loader, would be to move `xml-resolver` library to Tomcat `endorsed` folder, next to `xerces`, but some environments doesn't allow to change the `endorsed folder.

The library should be copy in Tomcat `lib` folder and requires defining a context file for GeoNetwork in Tomcat `conf/Catalina/localhost/geonetwork.xml`:

```
<?xml version='1.0' encoding='utf-8'?>
<Context>
  <Loader loaderClass="org.geonetwork.tomcat.GeoNetworkWebappLoader"
          useSystemClassLoaderAsParent="false"
          delegate="false"/>
</Context>
```

# Build the library

To build the library with the geonetwork war package, use the maven profile `tomcat-geonetwork-classloader`:

```
$  mvn clean install -Penv-prod,html5ui,tomcat-geonetwork-classloader 
```

To build the standalone library:

```
$ cd tomcat-geonetwork-classloader
$  mvn clean install
```

In the folder `tomcat-geonetwork-classloader\target` is available the library `tomcat-geonetwork-classloader-2.10.3-0.jar`.