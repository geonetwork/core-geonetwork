# GeoNetwork Architecture Overview

GeoNetwork is a modular application, managed using the spring framework, divided into clear application tiers.

![GeoNetwork Architecture](geonetwork-architecture.png "GeoNetwork Architecture")

GeoNetwork uses a wide range of technologies:

* [Angular](https://angular.io/) web framwork providing model-view-controller architecture with data model binding
* [Apache Lucene](https://lucene.apache.org) text index to facilitate searching
* [Bootstrap](https://getbootstrap.com/) front-end open source toolkit providing response page layout
* [Elasticsearch](https://www.elastic.co/elasticsearch/) to manage usage statistics and reporting
* [GeoTools](https://geotools.org/) Java GIS toolkit used to provide a spatial index
* [OpenLayers](http://openlayers.org) web mapping framework
* [Saxon](http://www.saxonica.com/) XSLT engine used extensively to process XML documents
* [Spring framework](https://spring.io/) Java web application framework used to "wire" components together

GeoNetwork is Java Web Application, requiring an application server to operate:

* [Jetty](https://www.eclipse.org/jetty/) application server for standaline distribution
* [Apache Tomcat](http://tomcat.apache.org/) application server for WAR distribution

GeoNetwork configuration is managed via:

* Configuration is managed via [Hibernate Object/Relational Mapping](https://hibernate.org/orm/) with [H2 Database Engine](https://www.h2database.com/) default for local testing, [PostgreSQL](https://www.postgresql.org/) recommended for production. Additional dialects are available for environments restricted to Oracle or SQLServer.

* Data Directory for the management of thumbnails, attachments and other application files
