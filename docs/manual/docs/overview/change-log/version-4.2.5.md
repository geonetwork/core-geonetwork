# Version 4.2.5 {#version-425}

GeoNetwork 4.2.5 release is a minor release.

## Migration notes

### Database changes

-   Remove Language table isdefault column ([More information](https://github.com/geonetwork/core-geonetwork/pull/7169)).

### API changes

-   API for link analysis
    -   `PUT /records` when using OVERWRITE mode only the XML of the metadata is updated. Use REMOVE_AND_REPLACE for previous mechanism which remove the existing metadata and inserting the new one. When workflow is enabled, OVERWRITE is not allowed ([More information](https://github.com/geonetwork/core-geonetwork/pull/7178)).

### Index changes

After update, don't forget to go to admin console --> tools --> Delete index and reindex.

### Thesaurus changes

If using the default regions thesaurus, [update the thesaurus dates before starting the application](https://github.com/geonetwork/core-geonetwork/pull/7208/files) ([More information](https://github.com/geonetwork/core-geonetwork/issues/7207)).

## List of changes

Major changes:

-   [Full text search / Add search by individual name and email](https://github.com/geonetwork/core-geonetwork/pull/7167)
-   [Map / Layer manager / Add support for multilingual layer title](https://github.com/geonetwork/core-geonetwork/pull/7121)
-   [RSS / Display RSS links when OGC API Records is installed](https://github.com/geonetwork/core-geonetwork/pull/7094)
-   [Workflow improvements](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.5+is%3Aclosed+workflow)
-   [CSW / DCAT / Fix for multilingual records](https://github.com/geonetwork/core-geonetwork/pull/7161)
-   [ATOM feeds / Support elements using anchor](https://github.com/geonetwork/core-geonetwork/pull/7156)
-   [SEO / Set HTML head title and description](https://github.com/geonetwork/core-geonetwork/pull/7080)
-   [DOI / Support European Union Publication Office format](https://github.com/geonetwork/core-geonetwork/pull/6979)
-   [User interface / Update Font-awesome icon library](https://github.com/geonetwork/core-geonetwork/pull/7007)
-   [Admin / Harvester / Fix log file access](https://github.com/geonetwork/core-geonetwork/pull/7127)
-   [Admin / Pages / Add support for submenu](https://github.com/geonetwork/core-geonetwork/pull/7138)
-   [Installation / Database / Add config property to turn off database creation/migration on startup](https://github.com/geonetwork/core-geonetwork/pull/7100)

and more \... see [4.2.5 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.5+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.2.5+is%3Aclosed) for full details.
