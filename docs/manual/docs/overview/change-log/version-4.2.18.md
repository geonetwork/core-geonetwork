# Version 4.2.18

GeoNetwork 4.2.18 is a minor release.

## Migration notes

Please note that GeoNetwork 4.2 is nearing end-of-life and you should plan your upgrade to GeoNetwork 4.4 at this time.

### API changes

- [Associated records / Align related records API filtering with associated records](https://github.com/geonetwork/core-geonetwork/pull/9498)
- [Unify error messages in user password recovery API](https://github.com/geonetwork/core-geonetwork/pull/9569)

### Installation changes

- [Proxy / Extend and normalise the list of hosts excluded from the proxy](https://github.com/geonetwork/core-geonetwork/pull/9626): the default `excludeHosts` list now includes private network ranges, and the new `followRedirects` setting (disabled by default) controls whether the proxy follows redirects. Redirects from http to https for the same URL are still followed. Catalogues that use the proxy to reach hosts on a private network need to review these settings.

### Settings changes

- A new system setting, **Thesaurus URL allowlist**, can restrict the URLs from which thesauri are imported. It is empty by default, so any URL is allowed.

## List of changes

- Map viewer
    - [Update OpenLayers to version 10.11.0](https://github.com/geonetwork/core-geonetwork/pull/9605)
    - [Fix 3D mode with OpenLayers 10](https://github.com/geonetwork/core-geonetwork/pull/9612)
    - [Add support for WMS 1.1.x capabilities](https://github.com/geonetwork/core-geonetwork/pull/9599)
- [Statistics and status page / Indexing status improvements](https://github.com/geonetwork/core-geonetwork/pull/9566)
- [Monitoring / Metrics filter and Jolokia improvements](https://github.com/geonetwork/core-geonetwork/pull/9591)
- [Static resources / Send Cache-Control: no-cache](https://github.com/geonetwork/core-geonetwork/pull/9623)
- [Metadata editor / Online resources / Fix mimeTypeStrategy parameter when adding a new online resource](https://github.com/geonetwork/core-geonetwork/pull/9464)
- [Harvester / Fix missing log file button in harvester history](https://github.com/geonetwork/core-geonetwork/pull/9565)
- [OAI-PMH / Check view privilege on GetRecord](https://github.com/geonetwork/core-geonetwork/pull/9444)
- [Keywords API improvements](https://github.com/geonetwork/core-geonetwork/pull/9624)
- [API / Feedback improvements](https://github.com/geonetwork/core-geonetwork/pull/9516)
- Documentation, MapFish print 2.2.1, Java and JS library updates

and more \... see [4.2.18-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.18+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.18+is%3Aclosed) for full details.
