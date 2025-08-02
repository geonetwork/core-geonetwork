# Version 4.4.8

GeoNetwork 4.4.8 is a minor release.

## Migration notes

### API changes

* [Restrict WFSHarvester API to users with Editor role](https://github.com/geonetwork/core-geonetwork/pull/8760)

### Security Advisory

Release is made in conjunction with a security advisory to be published early June.

## List of changes

New feature:

* [Allow to configure the Elasticsearch connection in environment variables](https://github.com/geonetwork/core-geonetwork/pull/8764)
* [Contact full view vs icon view configuration on record summary page](https://github.com/geonetwork/core-geonetwork/pull/8789)
* [Database harvester supporting PostgreSQL and Oracle JDBC connections](https://github.com/geonetwork/core-geonetwork/pull/8795)
* [Setting option to enable/disable backup during delete metadata record(s)](https://github.com/geonetwork/core-geonetwork/pull/8784)
* [Static page for record view menu](https://github.com/geonetwork/core-geonetwork/pull/8740)

Fixes:

* [Avoid null pointer when indexing overview data](https://github.com/geonetwork/core-geonetwork/pull/8736)
* [Fix bearer token API access from a different application](https://github.com/geonetwork/core-geonetwork/pull/8738)
* [Fix cannot update groups for static page](https://github.com/geonetwork/core-geonetwork/pull/8759)
* [Update to GeoTools 28.6.1](https://github.com/geonetwork/core-geonetwork/pull/8812)
* [WebDav harvester / update database change date when updating a metadata](https://github.com/geonetwork/core-geonetwork/pull/8790)

and more \... see [4.4.8-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.8+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?&q=is%3Apr+milestone%3A4.4.8+is%3Aclosed) for full details.
