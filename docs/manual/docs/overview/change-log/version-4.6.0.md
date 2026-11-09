# Version 4.6.0 {#version-460}

GeoNetwork 4.6.0 is a minor release. All requests are now served by Spring MVC instead of the Jeeves framework.

## Migration notes

When migrating from 4.4.x series:

### Removed services

The following services were removed. Use the REST API instead (see the API documentation at `/srv/api/doc`):

| Removed service | Replacement |
|---|---|
| `admin.harvester.*` (list, add, update, clone, remove, clear, start, stop, run, history, log) | `/api/harvesters` |
| `thesaurus`, `thesaurus.*` (browsing and keyword editing) | `/api/registries/vocabularies` |
| `admin.schematron*` (validation configuration) | `/api/schematron` |
| `versioning.logdata` | `/api/records/versions/log` |
| `lang` | `/api/isolanguages` |
| `rss.search` | No replacement. Use the OpenSearch or CSW endpoints. |
| `notifier.save`, `notifier.get` | Settings, `/api/site/settings` |

Links to `file.disclaimer` and `resources.get.archive` stored in records still work: they redirect to the record attachment (`/api/records/{uuid}/attachments/{file}`).

The user interface pages (`catalog.search`, `catalog.edit`, `admin.console`, ...), `csw`, `csw-publication` and `info` keep their URLs.

### Behaviour changes

-   Unknown `/srv/{language}/{service}` URLs return 404.
-   The debug suffix on page URLs (eg. `catalog.search!`, which returned the page XML) and `_content_type=json` on HTML pages are no longer supported.
-   Services declared in a `<services>` section of `WEB-INF/config.xml` are ignored. Custom CSW endpoints declared this way (`csw-xxx`) are no longer supported: use a [sub-portal](../../administrator-guide/configuring-the-catalog/portal-configuration.md) instead.
-   When the catalogue is in read-only mode (database not writable), API requests that modify data return HTTP 503, and `csw-publication` returns an exception report.
-   The `robots.txt` file no longer advertises the RDF sitemap (`api/sitemap?format=rdf`), which returned the HTML sitemap.

## List of changes

-   Harvester administration, thesaurus editing, schematron configuration and version history use the REST API.
-   A user administrator can only manage the harvesters of their groups.
-   CSW, the user interface pages and `info` are served by Spring MVC with the same URLs and responses.
-   The error, node change and OpenSearch description pages no longer redirect to the sign in page.
-   The OpenSearch description and DCAT catalogue descriptions use CSW with DCAT output instead of the removed `rdf.search`; CSW `GetRecords` returns ISO 19115-3 records in DCAT.
