# Software Development

This manual is for software developers customizing or developing GeoNetwork:

* [GeoNetwork Architecture Overview](OVERVIEW.md) describing technologies used and application components.
* Development environment:

  * [Tools](TOOLS.md)  
  * [Source code](SOURCE.md)
  * [GitHub](GITHUB.md)
  * [Building](BUILDING.md)
  * [Testing](TESTING.md)
  * [Eclipse IDE](ECLIPSE.md)
  * [IntelliJ IDE](INTELLIJ.md).
  
  See [Installing from source code](https://geonetwork-opensource.org/manuals/trunk/en/maintainer-guide/installing/installing-from-source-code.html) (Maintainer Guide)

* Writing documentation: see [Writing documentation](https://geonetwork-opensource.org/manuals/trunk/en/contributing/writing-documentation.html) (Contributing Guide).
* Release process: See [Doing a release](https://geonetwork-opensource.org/manuals/trunk/en/contributing/doing-a-release.html) (Contributing Guide).


If you just want to use the software and are looking for instructions on how to do that,
there is a lot of documentation for users,  administrators, metadata editors and application
maintainers at: [GeoNetwork Documentation](http://geonetwork-opensource.org/manuals/trunk/eng/users/index.html).

# Quickstart

Get GeoNetwork running - the short path:

```
git clone --depth 3 --recursive https://github.com/geonetwork/core-geonetwork.git
cd core-geonetwork
mvn clean install -DskipTests
cd web
mvn jetty:run
```

The application is opened on http://localhost:8080/geonetwork 
