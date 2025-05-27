# Version 4.2.12

GeoNetwork 4.2.12 is a minor release.

## Migration notes

### API changes

* [Add get /api/sources/{sourceIdentifier} to be able to get a source by id.](https://github.com/geonetwork/core-geonetwork/pull/8629)
* [Update getRecordStatusHistory and getRecordStatusHistoryByType to support getting working copy records history.](https://github.com/geonetwork/core-geonetwork/pull/8620)

### Installation changes

### Index changes

* [Record view / DQ / Add measure date information](https://github.com/geonetwork/core-geonetwork/pull/8593)
* [Record view / Lineage & Quality section improvements (#7180)](https://github.com/geonetwork/core-geonetwork/pull/8590)

## List of changes

New feature:

* [Component to allow checking duplicated metadata values for title, alternative title and resource identifier in the metadata editor and display a message to the user](https://github.com/geonetwork/core-geonetwork/pull/8516)

Major changes:

* [Fix harvesting errors not always being reported](https://github.com/geonetwork/core-geonetwork/pull/86900)
* [Record view / Lineage & Quality section improvements (#7180)](https://github.com/geonetwork/core-geonetwork/pull/8590)
* [Update index to use a key with translations defined for map resource types](https://github.com/geonetwork/core-geonetwork/pull/8568)
* [Move datastorage providers to maven modules and include them in the build on demand](https://github.com/geonetwork/core-geonetwork/pull/8561)

Fixes:

* [URL Decode filename for resources uploaded by url](https://github.com/geonetwork/core-geonetwork/pull/8641)
* [Fix privileges cannot be unset](https://github.com/geonetwork/core-geonetwork/pull/8611)
* [Url decode the resourceId supplied to store.getResourceInternal to fix filenames containing spaces.](https://github.com/geonetwork/core-geonetwork/pull/8587)
* [Use the copyBlob to copy the resource with updated metadata](https://github.com/geonetwork/core-geonetwork/pull/8533)
* [Update file upload so that it has better error support.](https://github.com/geonetwork/core-geonetwork/pull/8513)

and more \... see [4.2.12-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.12+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.12+is%3Aclosed) for full details.
