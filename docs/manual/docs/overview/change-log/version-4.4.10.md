# Version 4.4.10

GeoNetwork 4.4.10 is a minor release.

## Migration notes

No migration is needed for this release.

### List of changes

API changes:

* [Limit zip export resources size and add option to exclude attachments](https://github.com/geonetwork/core-geonetwork/pull/9130) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [Allow editors to index their records](https://github.com/geonetwork/core-geonetwork/pull/9058) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [Improve validation response](https://github.com/geonetwork/core-geonetwork/pull/8985) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [Make userIdentifier optional in user selection api](https://github.com/geonetwork/core-geonetwork/pull/8864) by [ianwallen](https://github.com/ianwallen)


Index structure change:

* [Add support to get metadata resource json object into the index.](https://github.com/geonetwork/core-geonetwork/pull/9036) by [ianwallen](https://github.com/ianwallen)


Features:

* [Public permalink to private md](https://github.com/geonetwork/core-geonetwork/pull/9059) by [cmangeat](https://github.com/cmangeat)

* [New metadata form / Don't exclude templates with resource type map-static](https://github.com/geonetwork/core-geonetwork/pull/9217) by [josegar74](https://github.com/josegar74)

* [Editor user can not change privileges when the metadata is published.](https://github.com/geonetwork/core-geonetwork/pull/9197) by [josegar74](https://github.com/josegar74)

* [Add scrollable dropdown for WFS format selection menu](https://github.com/geonetwork/core-geonetwork/pull/9184) by [juanluisrp](https://github.com/juanluisrp)

* [Add UUID display to directory entries](https://github.com/geonetwork/core-geonetwork/pull/9182) by [cmangeat](https://github.com/cmangeat)

* [Enable group owner access to new field](https://github.com/geonetwork/core-geonetwork/pull/9177) by [tanzeeladebont](https://github.com/tanzeeladebont)

* [Metadata delete dialog / Allow to display a custom message, based on the metadata resource type(s) to delete.](https://github.com/geonetwork/core-geonetwork/pull/9172) by [josegar74](https://github.com/josegar74)

* [Metadata delete dialog](https://github.com/geonetwork/core-geonetwork/pull/9144) by [PascalLike](https://github.com/PascalLike)

* [Formatter / Use portal filter (rebased #8263)](https://github.com/geonetwork/core-geonetwork/pull/9139) by [davidblasby](https://github.com/davidblasby)

* [Add support for reindexing all metadata of a harvester](https://github.com/geonetwork/core-geonetwork/pull/9133) by [tobias-hotz](https://github.com/tobias-hotz)

* [New postgres-postgis config file with an hikari cp connection pool.](https://github.com/geonetwork/core-geonetwork/pull/9107) by [cmangeat](https://github.com/cmangeat)

* [Update Elasticsearch to v8.19.13](https://github.com/geonetwork/core-geonetwork/pull/9176) by [josegar74](https://github.com/josegar74)

* [Fix horizontal scroll bar in WFS features table](https://github.com/geonetwork/core-geonetwork/pull/9160) by [juanluisrp](https://github.com/juanluisrp)

* [FIX: GeoNetwork 4 harvester missing one record due to pagination bug](https://github.com/geonetwork/core-geonetwork/pull/9154) by [juanluisrp](https://github.com/juanluisrp)

* [Add the option to sort search results by title in descending order](https://github.com/geonetwork/core-geonetwork/pull/9226) by [josegar74](https://github.com/josegar74)

* [Add support for API Key in harvesters](https://github.com/geonetwork/core-geonetwork/pull/9094) by [PascalLike](https://github.com/PascalLike)

* [Additional filter options in GN 4.x harvesters](https://github.com/geonetwork/core-geonetwork/pull/9100) by [juanluisrp](https://github.com/juanluisrp)

* [Add option to skip attachments on record duplication](https://github.com/geonetwork/core-geonetwork/pull/9021) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [Stream metadata resources](https://github.com/geonetwork/core-geonetwork/pull/9013) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [Schema fixes 2 (other schema fixes)](https://github.com/geonetwork/core-geonetwork/pull/9009) by [cmangeat](https://github.com/cmangeat)

* [Add formatter implementation for DCAT supporting turtle and json-ld formats](https://github.com/geonetwork/core-geonetwork/pull/8908) by [josegar74](https://github.com/josegar74)

* [Implement Group Types](https://github.com/geonetwork/core-geonetwork/pull/8741) by [tylerjmchugh](https://github.com/tylerjmchugh)

* [File uploads / check mimetypes](https://github.com/geonetwork/core-geonetwork/pull/7710) by [josegar74](https://github.com/josegar74)

* [Metadata editor - WMS resources - allow to configure the WMS layer info to add to the resource name and resource description fields in the url mode.](https://github.com/geonetwork/core-geonetwork/pull/6465) by [josegar74](https://github.com/josegar74)

* [Restrict username / group name characters](https://github.com/geonetwork/core-geonetwork/pull/6001) by [josegar74](https://github.com/josegar74)

* [Allow to customise the cookie warning template](https://github.com/geonetwork/core-geonetwork/pull/9222) by [josegar74](https://github.com/josegar74)

* [Improve CKAN Harvester by converting HTML to Plain Text](https://github.com/geonetwork/core-geonetwork/pull/9101) by [tobias-hotz](https://github.com/tobias-hotz)

* [STAC harvester DOI support](https://github.com/geonetwork/core-geonetwork/pull/9089) by [cmangeat](https://github.com/cmangeat)

* [Update OpenLayers library to version 10.8.0](https://github.com/geonetwork/core-geonetwork/pull/9225) by [josegar74](https://github.com/josegar74)

* [Update Mapfish print](https://github.com/geonetwork/core-geonetwork/pull/9075) by [fxprunayre](https://github.com/fxprunayre)


and more \... see [4.4.10 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.10+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.4.10+is%3Aclosed) for full details.
