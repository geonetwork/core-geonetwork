# Software Development

This manual is for developers customizing or developing GeoNetwork:

* [Overview](OVERVIEW.md) describing architecture, technologies, and libraries used.
* Development environment:

  * [Tools](TOOLS.md)  
  * [Source code](SOURCE.md)
  * [GitHub](GITHUB.md)
  * [Building](BUILDING.md)
  * [Testing](TESTING.md)
  * [Eclipse IDE](ECLIPSE.md)
  * [IntelliJ IDE](INTELLIJ.md)

* Installation: See [Installing from source code](https://docs.geonetwork-opensource.org/latest/install-guide/installing-from-source-code/) (Installation Guide).
* Writing documentation: See [Working with documentation](https://docs.geonetwork-opensource.org/latest/devel/docs/) (Documentation Guide).
* Release process: See [Doing a release](https://docs.geonetwork-opensource.org/latest/contributing/doing-a-release/) (Contributing Guide).

If you just want to use the software and are looking for instructions on how to do that,
there is a lot of documentation for users,  administrators, metadata editors and application
maintainers at: [GeoNetwork Documentation](https://docs.geonetwork-opensource.org/latest/).

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

