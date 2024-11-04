# Version 3.12.12

GeoNetwork 3.12.12 is a minor release.

## List of changes

- Metadata
    - [BP] [Fix exception handling from schematron validation so that it flags the metadata as invalid if there is an exception](https://github.com/geonetwork/core-geonetwork/pull/6978)
    - [BP] [Overview not shown in PDF export when the overview image is stored in GeoNetwork and requires authentication to access it](https://github.com/geonetwork/core-geonetwork/pull/7556)

- Administration
    - [BP] [Harvesters / Reset harvester history pagination when selecting a harvester](https://github.com/geonetwork/core-geonetwork/pull/7836)
    - [BP] [GeoNetwork harvester / Check if a resource exists to save it, instead of trying to retrieve the file details, to avoid confusing NoSuchFileException exception](https://github.com/geonetwork/core-geonetwork/pull/7846)

- Other
    - [BP] [Fix cookies path when deployed on root "/" context](https://github.com/geonetwork/core-geonetwork/pull/7446)
    - [BP] [Remove exception class name from the error message](https://github.com/geonetwork/core-geonetwork/pull/6977)
    - Update `org.json:json` from version 20140107 to 20240205
    - Update `commons-fileupload` from version 1.3.3 to 1.5
    - Documentation / Manual improvements

and more \... see [3.12.12 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A3.12.12+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A3.12.12+is%3Aclosed) for full details.

**Full Changelog**: [here](https://github.com/geonetwork/core-geonetwork/compare/3.12.11...3.12.12)
