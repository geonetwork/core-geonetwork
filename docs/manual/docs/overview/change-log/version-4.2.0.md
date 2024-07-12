# Version 4.2.0 {#version-420}

GeoNetwork 4.2.0 release is a major release.

## Migration instructions

Due to H2 database major update, when migrating from a previous version drop the following database caches:

-   JS/CSS cache database in \$DATA_DIR/wro4j-cache.mv.db
-   Formatter cache database in \$DATA_DIR/data/resources/htmlcache/formatter-cache/info-store.mv.db

If using H2 as the main database consider migrating to an external database (see [Configuring the database](../../install-guide/configuring-database.md)) or read [H2 migration guide](https://www.h2database.com/html/migration-to-v2.html) and migrate the database to version 2 format.

Then start the application.

If using GeoNetwork 4.2.0 with an H2 version 1 database, the following error will be reported by H2 driver:

``` 
General error: "The write format 1 is smaller than the supported format 2 [2.1.212/5]" [50000-212]
```

## List of changes

Major changes:

-   [Library updates](https://github.com/geonetwork/core-geonetwork/pull/6244) including the move to H2 version 2 (not compatible with H2 version 1)
-   [Search / Multilingual support](https://github.com/geonetwork/core-geonetwork/pull/6188)
-   [Search / Display results as table](https://github.com/geonetwork/core-geonetwork/pull/6170)
-   [Search / Associated resources can now be part of the search response API](https://github.com/geonetwork/core-geonetwork/pull/6269)
-   [Record view / Improved navigation](https://github.com/geonetwork/core-geonetwork/pull/6188)
-   [Record view / Dataset citation formatter](https://github.com/geonetwork/core-geonetwork/pull/6188)
-   [Toolbar / Add menu to switch portals](https://github.com/geonetwork/core-geonetwork/pull/6256)
-   [Editing / Define preferred template and group](https://github.com/geonetwork/core-geonetwork/pull/6128) to easily create new records
-   [Editing / Database search and replace](https://github.com/geonetwork/core-geonetwork/pull/6188)
-   [Editing / Batch editing examples and copy/paste facilities](https://github.com/geonetwork/core-geonetwork/pull/6239)
-   [Editing / Use directories for ISO19115-3 records](https://github.com/geonetwork/core-geonetwork/pull/6292) [Managing directories](../../administrator-guide/managing-classification-systems/managing-directories.md)
-   [Editing / Easily register service in Spatineo monitor for ISO19115-3](https://github.com/geonetwork/core-geonetwork/pull/6298)
-   [INSPIRE / Configure API endpoint and gateway URL](https://github.com/geonetwork/core-geonetwork/pull/6146)
-   [Admin / Languages / Easily remove unneeded languages](https://github.com/geonetwork/core-geonetwork/pull/5923) to keep admin form as simple as possible
-   [Admin / Harvesting / Order options according to processing](https://github.com/geonetwork/core-geonetwork/pull/6221) to better understand the harvesting steps
-   [Admin / Harvesting / JSON file support](https://github.com/geonetwork/core-geonetwork/pull/6251)
-   [Admin / Thesaurus / Improved form and add support for description](https://github.com/geonetwork/core-geonetwork/pull/6283)
-   [Admin / Link analysis / Filter by HTTP status.](https://github.com/geonetwork/core-geonetwork/pull/6255) Those status gives more details, a link could be valid with an authorization required status.

and more \... see [4.2.0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.0+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.0+is%3Aclosed) for full details.
