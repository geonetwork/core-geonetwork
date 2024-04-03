# Version 4.4.3 {#version-423}

GeoNetwork 4.4.3 release is a minor release.

## Migration notes

### Java

**Version 4.4 only works on Java 11.**

### Index changes

This version use Elasticsearch version 8 Java client, it is recommended to use an Elasticsearch version 8 server.
However version 7.15+ and 8+ have been tested.

After update, don't forget to go to admin console --> tools --> Delete index and reindex.

### Map

[Stamen background layers are not available, update your maps](https://github.com/geonetwork/core-geonetwork/pull/7715).


## List of changes

Major changes:

- [Elasticssearch 8 upgrade](https://github.com/geonetwork/core-geonetwork/pull/7599)
- [Editor / Distribution panel improvements](https://github.com/geonetwork/core-geonetwork/pull/7468)
- [Thesaurus / Add support for codelist described using SDMX](https://github.com/geonetwork/core-geonetwork/pull/7790)
- [Thesaurus / Add support for thesaurus described using OWL format](https://github.com/geonetwork/core-geonetwork/pull/7674)
- [Thesaurus / Improve support of EU publication office SKOS format](https://github.com/geonetwork/core-geonetwork/pull/7673)
- [INSPIRE / Add testsuite for IACS](https://github.com/geonetwork/core-geonetwork/pull/7756)
- [Map viewer / Remove Stamen background layers - no longer available](https://github.com/geonetwork/core-geonetwork/pull/7715)
- [i18n / Add welsh language for user interface](https://github.com/geonetwork/core-geonetwork/pull/7851)
- [Index / Add danish language configuration](https://github.com/geonetwork/core-geonetwork/pull/7697)
- [Index / Translated the index warnings and errors](https://github.com/geonetwork/core-geonetwork/pull/7531)
- [Create a metadata / Add dynamic and download privileges to the users in the same group](https://github.com/geonetwork/core-geonetwork/pull/7744)
- [Decouple metadata user feedback from metadata rating feature](https://github.com/geonetwork/core-geonetwork/pull/7796)
- [Extend http proxy to manage duplicated parameters](https://github.com/geonetwork/core-geonetwork/pull/7854)
- [Fix MIME-types on attachments](https://github.com/geonetwork/core-geonetwork/pull/7675)
- [Fix pdf link to the application website](https://github.com/geonetwork/core-geonetwork/pull/7681)
- Update `org.json:json` from version 20140107 to 20240205
- Documentation / Manual improvements
- Documentation / API SpringDoc fixes

and more \... see [4.4.3 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.3+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.4.3+is%3Aclosed) for full details.
