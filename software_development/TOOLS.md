# Tools

GeoNetwork is a Java Web Application, developed using Java and Maven.

Documentation makes use of the python Sphinx build system.

## Git

GeoNetwork uses [Git](https://git-scm.com/) version control.

Our source code is maintained on GitHub:

* [github.com/geonetwork](https://github.com/geonetwork):

  * [core-geonetwork](https://github.com/geonetwork/core-geonetwork)
  * [doc](https://github.com/geonetwork/doc)

* [github.com/metadata101](https://github.com/metadata101):

  * [iso19115-3.2018](https://github.com/metadata101/iso19115-3.2018)

## Java 11

GeoNetwork requires a Java 11 development environment:

* Recommend using the Java 11 distribution available from your operating system
* [Adoptium OpenJDK8 (LTS)](https://adoptium.net/temurin/archive/?version=11) for windows or macOS installers

We no longer support Java 8 at this time.

## Elasticsearch

GeoNetwork uses Elasticsearch as a full-text search engine.

```
cd es
docker-compose up
```

* Elasticsearch: http://localhost:9200
* Kibana: http://localhost:5601

GeoNetwork Reference:

* [es/readme](../es/README.md)

Reference:

* https://www.elastic.co/guide/index.html

## Apache Maven

We make use of the Apache Maven [Apache Maven](https://maven.apache.org/) build system.

To build and run:
```
mvn clean install -DskipTests
cd web
mvn jetty:run
```

* GeoNetwork: http://localhost:8080/geonetwork

Maven repository is available at repo.osgeo.org:

* https://repo.osgeo.org/repository/release/
* https://repo.osgeo.org/repository/snapshot/

GeoNetwork Reference

* [software_development/building](BUILDING.md)
* [web](../web/README.md)

Reference:

* https://maven.apache.org/index.html

## Material for MkDocs

Documentation is built using [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) theme, built using python ***mkdocs*** generator.

```
cd docs/mannual
pip3 install -r requirements.txt
mkdocs serve
```

GeoNetwork Reference:

* [docs/manual](../docs/manual/README.md)

Reference:

* https://squidfunk.github.io/mkdocs-material/
* https://www.mkdocs.org
