# Version 4.4.7

GeoNetwork 4.4.7 is a minor release.

## Migration notes

### API changes

* [Fix set site settings in swagger_ui so it function correctly.](https://github.com/geonetwork/core-geonetwork/pull/8582)
* [Editor / Add keywords even if thesaurus not available in record languages](https://github.com/geonetwork/core-geonetwork/pull/8268)
* [Add get /api/sources/{sourceIdentifier} to be able to get a source by id.](https://github.com/geonetwork/core-geonetwork/pull/8266)
* [Update getRecordStatusHistory and getRecordStatusHistoryByType to support getting working copy records history.](https://github.com/geonetwork/core-geonetwork/pull/8153)

### Installation changes

### Index changes

* [Add Polish translations from Transifex and update the translations for the other languages](https://github.com/geonetwork/core-geonetwork/pull/8612)
* [ISO19110 / ISO19115-3.2008 / Fix indexing of multiple feature type aliases](https://github.com/geonetwork/core-geonetwork/pull/8545)

## List of changes

Major changes:

* [Allow deploying a Datahub instance (GeoNetwork-UI) from the administration interface](https://github.com/geonetwork/core-geonetwork/pull/8644)
* [Fix harvesting errors not always being reported](https://github.com/geonetwork/core-geonetwork/pull/8647)
* [Update to GeoTools 32.2](https://github.com/geonetwork/core-geonetwork/pull/8613)
* [Add Polish translations from Transifex and update the translations for the other languages](https://github.com/geonetwork/core-geonetwork/pull/8612)
* [Configuration to display an application banner](https://github.com/geonetwork/core-geonetwork/pull/8416)
* [Improve performance of parsing simple dates](https://github.com/geonetwork/core-geonetwork/pull/8386)
* [Feature catalogue / Add table of content of feature types](https://github.com/geonetwork/core-geonetwork/pull/8041)

Fixes:

* [URL Decode filename for resources uploaded by url](https://github.com/geonetwork/core-geonetwork/pull/8628)
* [Fix privileges cannot be unset](https://github.com/geonetwork/core-geonetwork/pull/8609)
* [Url decode the resourceId supplied to store.getResourceInternal to fix filenames containing spaces.](https://github.com/geonetwork/core-geonetwork/pull/8581)
* [Use the copyBlob to copy the resource with updated metadata](https://github.com/geonetwork/core-geonetwork/pull/8530)
* [Update index to use a key with translations defined for map resource types](https://github.com/geonetwork/core-geonetwork/pull/8529)
* [Update db search and replace to support working copies](https://github.com/geonetwork/core-geonetwork/pull/8514)
* [Directory entries / Fix parsing of sorting configuration](https://github.com/geonetwork/core-geonetwork/pull/8482)
* [Update file upload so that it has better error support.](https://github.com/geonetwork/core-geonetwork/pull/8427)

and more \... see [4.4.7-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.7+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.4.7+is%3Aclosed) for full details.
