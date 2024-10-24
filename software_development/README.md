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
  
  See [Installing from source code](https://docs.geonetwork-opensource.org/4.2/install-guide/installing-from-source-code/) (Installation Guide)

* Writing documentation: see [Documentation Guide](https://docs.geonetwork-opensource.org/latest/devel/docs/) (Contributing Guide).
* Release process: See [Doing a release](https://docs.geonetwork-opensource.org/4.2/contributing/doing-a-release/) (Contributing Guide).


If you just want to use the software and are looking for instructions on how to do that,
there is a lot of documentation for users,  administrators, metadata editors and application
maintainers at: [GeoNetwork Documentation](https://docs.geonetwork-opensource.org/4.2/).

# Quickstart

Get GeoNetwork running - the short path:

1. Build:
   ```
   git clone --depth 3 --recursive https://github.com/geonetwork/core-geonetwork.git
   cd core-geonetwork
   mvn clean install -DskipTests
   ```

2. Elasticsearch:
   ```
   cd es
   mvn install -Pes-download
   mvn exec:exec -Des-start
   ```

3. GeoNetwork web application:
   ```
   cd web
   mvn jetty:run
   ```

4. The application is opened on http://localhost:8080/geonetwork 
