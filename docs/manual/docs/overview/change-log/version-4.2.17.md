# Version 4.2.17

GeoNetwork 4.2.17 is a minor release.

## Security Considerations

This release includes a security fix for a vulnerability affecting previous 4.2.x versions. **All users are encouraged to update to 4.2.17 as soon as possible.**


* [CVE-2026-63219](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-mh22-prqr-vf42) Unauthenticated file upload via missing authorization on formatter upload endpoint 
* [CVE-2026-55864](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-5hx7-j24v-rffj) Unauthenticated Server-Side Request Forgery in SLD Tool 
* [CVE-2026-58400](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-x898-729x-cc3r) Remote Code Execution via unsafe Saxon XSLT processor configuration in formatter
* [CVE-2026-69130](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-jcv2-3cfh-v9h2) Map feature popup renders KML/vector description and attributes as raw HTML via innerHTML

## Migration notes

Please note that GeoNetwork 4.2 is nearing end-of-life and you should plan your upgrade to GeoNetwork 4.4 at this time.

### API changes

- [Formatter limits](https://github.com/geonetwork/core-geonetwork/pull/9411)
- [Map viewer / WFS filter / remove unused SLD API endpoints](https://github.com/geonetwork/core-geonetwork/pull/9344)

## List of changes

- [Fix delete users with metadata user feedback entries associated](https://github.com/geonetwork/core-geonetwork/pull/9356)
- [Formatter limits](https://github.com/geonetwork/core-geonetwork/pull/9411)
- [Formatter API consistent access control](https://github.com/geonetwork/core-geonetwork/pull/9347)
- [Map viewer / Sanitize layer attribution and popover](https://github.com/geonetwork/core-geonetwork/pull/9400)
- [Harvester / Fix when updating records](https://github.com/geonetwork/core-geonetwork/pull/9405)
- Java and JS library updates

and more \... see [4.2.17-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.2.17+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.2.17+is%3Aclosed) for full details.
