# Version 4.2.15

GeoNetwork 4.2.15 is a minor release.

## List of changes

Features:

- [Backport 4.2.x] Update OpenLayers library to version 10.8.0 (#9239)
- Allow to customise the cookie warning template (#9223)
- [Backport 4.2.x] ISO19139 / ISO19115-3.2018 / Full record view improvements (#9198)
- [Backport 4.2.x] Additional filter options in GN 4.x harvesters (#9104)
- [Backport 4.2.x] Add support for API Key in harvesters (#9096)
- [BP] API / Allow editors to index their records   (#9085)
- [Backport 4.2.x] Restrict username / group name characters (#9083)


Fixes:

- Modify URI construction logic in ThesaurusService (#9241)
- Map viewer / ignore layer dimensions for non WMS layers (#9237)
- Fix path for encryptor.properties file (#9162)
- ISO19115-3.2018 / Configure the xpath for the feature catalogue metadata titles (#9190)
- [Backport 4.2.x] FIX: GeoNetwork 4 harvester missing one record due to pagination bug
- [Backport 4.2.x] Harvesters / Check if the metadata schema of a metadata to update has changed (#9126)
- [Backport 4.2.x] Documentation for API Key in SimpleURL and CSW harvesters (#9098)
- [Backport 4.2.x] Fix errors with GN4 type harvester (#9093)
- [Backport 4.2.x] Skip BOM chars from XML documents (#9067)


and more \... see [4.2.15 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.15+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.2.15+is%3Aclosed) for full details.
