# Version 4.2.4 {#version-424}

GeoNetwork 4.2.4 release is a minor release.

## Migration notes

### Database changes

-   If using the pages API, consider checking the `spg_page` database table ([More information](https://github.com/geonetwork/core-geonetwork/pull/7005)) during migration.

### API changes

-   API for link analysis
    -   `POST /links` is now `POST /links/analyze` to analyze all links in a set of records.
    -   `POST /links/analyze` to `POST /links/analyzeurl` to analyze a URL.
    -   `POST /links` allows now queries with large filter which was not supported using GET ([More information](https://github.com/geonetwork/core-geonetwork/pull/7022)).
-   API on pages is now more consistent ([More information](https://github.com/geonetwork/core-geonetwork/pull/6788)). Use `GET pages` to retrieve list of pages (instead of `GET pages/list`). Use `PUT` operation to create a new page or `POST` to upload a file for the page. Section and status can now updated when updating the page. A page label can now be defined. Format, sections and status can now be retrieved using the API.

![](img/424-pagesapi.png)

## List of changes

Major changes:

-   Security / Library updates: [Spring](https://github.com/geonetwork/core-geonetwork/pull/7023), [SVN kit](https://github.com/geonetwork/core-geonetwork/pull/7017), [GeoTools](https://github.com/geonetwork/core-geonetwork/pull/6925), [Logging bridge](https://github.com/geonetwork/core-geonetwork/pull/6904)
-   [Languages / Fix loading of ICE, KOR and CZE language code](https://github.com/geonetwork/core-geonetwork/pull/7055) and [Danish added](https://github.com/geonetwork/core-geonetwork/pull/6933)
-   [Sharing / Configure profile allowed to publish/unpublish records](https://github.com/geonetwork/core-geonetwork/pull/6956)
-   [Sharing / Improve definition of the Intranet group](https://github.com/geonetwork/core-geonetwork/pull/6894)
-   [Portal / Check that portal exist and if not redirect to main one](https://github.com/geonetwork/core-geonetwork/pull/7034)
-   [Map / WMS Time and elevation support](https://github.com/geonetwork/core-geonetwork/pull/6820)
-   CSW / Transactions: [Record history of transactions](https://github.com/geonetwork/core-geonetwork/pull/7016) and [consistently apply update fixed info](https://github.com/geonetwork/core-geonetwork/pull/7004)
-   [Authentication / Easier extension of OpenID mode](https://github.com/geonetwork/core-geonetwork/pull/6965)
-   [Search / Improve performances for catalogs containing lot of overviews](https://github.com/geonetwork/core-geonetwork/pull/6895)
-   [CMIS / Performance improvements](https://github.com/geonetwork/core-geonetwork/pull/6893)
-   [Admin console / Pages manager](https://github.com/geonetwork/core-geonetwork/pull/6788)
-   [Editor configuration / Add for each support](https://github.com/geonetwork/core-geonetwork/pull/6907)

and more \... see [4.2.4 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.4+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.4+is%3Aclosed) for full details.
