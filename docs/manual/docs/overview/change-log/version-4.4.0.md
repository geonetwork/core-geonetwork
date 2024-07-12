# Version 4.4.0 {#version-440}

GeoNetwork 4.4.0 release is a major release.

## Migration notes

When migrating from 4.2.x series:

### Java

**Version 4.4 only works on Java 11.**

### API changes

-   Groovy formatter support is removed. ([More information](https://github.com/geonetwork/core-geonetwork/pull/7346)).

### Installation changes

-   Datastore / [To use CMIS, JCloud or S3 download the corresponding package](https://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v4.4.0/).

### Index changes

After update, don't forget to go to admin console --> tools --> Delete index and reindex.

## List of changes

Major changes:

-   [Java 11 support](https://github.com/geonetwork/core-geonetwork/pull/7186)
-   [Move datastorage providers to maven modules and include them in the build on demand](https://github.com/geonetwork/core-geonetwork/pull/7302)
-   [Docker configuration improvements](https://github.com/geonetwork/docker-geonetwork/pull/107)
-   [Improvements to allow future deployment of multiple instances](https://github.com/geonetwork/core-geonetwork/pull/7337)
-   [API / CSV export / Add support for custom export](https://github.com/geonetwork/core-geonetwork/pull/7132)
-   [INSPIRE / Validator / Add API usage information](https://github.com/geonetwork/core-geonetwork/pull/7284)
-   [Map / WMS / Add support for time dimension](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.4.0+is%3Aclosed+WMS)
-   [Map / WFS Features / Improvements](https://github.com/geonetwork/core-geonetwork/pull/7000)

  
and more \... see [4.4.0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.0+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.4.0+is%3Aclosed) for full details.
