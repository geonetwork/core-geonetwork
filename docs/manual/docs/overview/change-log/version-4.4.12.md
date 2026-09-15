# Version 4.4.12

GeoNetwork 4.4.12 is a minor release.

## Security Considerations

This release includes a security fix for a vulnerability affecting previous 4.4.x versions. **All users are strongly encouraged to update to 4.4.12 as soon as possible.**

* [CVE-2026-63219](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-mh22-prqr-vf42) Unauthenticated file upload via missing authorization on formatter upload endpoint 
* [CVE-2026-55864](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-5hx7-j24v-rffj) Unauthenticated Server-Side Request Forgery in SLD Tool 
* [CVE-2026-57582](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-5pq9-ppfw-p83j) Reflected XSS via unsanitized Javascript Sink 
* [CVE-2026-58400](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-x898-729x-cc3r) Remote Code Execution via unsafe Saxon XSLT processor configuration in formatter
* [CVE-2026-69130](https://github.com/geonetwork/core-geonetwork/security/advisories/GHSA-jcv2-3cfh-v9h2) Map feature popup renders KML/vector description and attributes as raw HTML via innerHTML

## Migration notes

### API changes

- [Map viewer / WFS filter / remove unused SLD API endpoints](https://github.com/geonetwork/core-geonetwork/pull/9343)

## List of changes

- [Index / Add Swedish language](https://github.com/geonetwork/core-geonetwork/pull/9355)
- [Formatter limits](https://github.com/geonetwork/core-geonetwork/pull/9408)
- [Formatter API consistent access control](https://github.com/geonetwork/core-geonetwork/pull/9346)
- [Harvester / Fix when updating records](https://github.com/geonetwork/core-geonetwork/pull/9403)
- [Harvester / CSW / GetRecords fails when requesting an outputSchema not supported by a metadata schema](https://github.com/geonetwork/core-geonetwork/pull/6941)
- [Map viewer / Sanitize layer attribution and popover](https://github.com/geonetwork/core-geonetwork/pull/9382)
- [OAI-PMH / Apply privileges filter](https://github.com/geonetwork/core-geonetwork/pull/9364)
- [Admin user can't publish/unpublish metadata](https://github.com/geonetwork/core-geonetwork/pull/9362)
- Java and JS library updates

and more \... see [4.4.12-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.12+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.4.12+is%3Aclosed) for full details.
