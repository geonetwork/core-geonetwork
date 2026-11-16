# Version 4.4.13

GeoNetwork 4.4.13 is a minor release.

## Security Considerations

This release includes a security fix for a vulnerability affecting previous 4.4.x versions. **All users are strongly encouraged to update to 4.4.13 as soon as possible.**

## Migration notes

### Z39.50 / SRU server removed

The SRU endpoint has been removed with its JZKit dependencies. See [Remove the Z39.50 / SRU server](https://github.com/geonetwork/core-geonetwork/pull/9485).

### Elasticsearch asynchronous indexing

Indexing and deletion requests to Elasticsearch are now handled asynchronously. See [Elasticsearch: Async handling of indexing/deletion requests](https://github.com/geonetwork/core-geonetwork/pull/8465).

### API changes

- [Remove the Z39.50 / SRU server](https://github.com/geonetwork/core-geonetwork/pull/9485)
- [API / Improve OpenAPI spec schema](https://github.com/geonetwork/core-geonetwork/pull/8602)
- [Fix issues with openapi spec](https://github.com/geonetwork/core-geonetwork/pull/9429)
- [Fix duplicate getDoiServers operationId](https://github.com/geonetwork/core-geonetwork/pull/9557)
- [API / OpenAPI endpoint return error when Accept is */*](https://github.com/geonetwork/core-geonetwork/pull/9200)
- [Unify error messages in user password recovery API](https://github.com/geonetwork/core-geonetwork/pull/9507)
- [Associated records / Align related records API filtering with associated records](https://github.com/geonetwork/core-geonetwork/pull/9437)
- [Get all versions of a record](https://github.com/geonetwork/core-geonetwork/pull/9227)
- [API / Feedback improvements](https://github.com/geonetwork/core-geonetwork/pull/9451)
- [Fix attachments endpoint](https://github.com/geonetwork/core-geonetwork/pull/9473)
- [Keywords API improvements](https://github.com/geonetwork/core-geonetwork/pull/9377). A new system setting, **Thesaurus URL allowlist**, can restrict the URLs from which thesauri are imported. It is empty by default, so any URL is allowed.

### Installation changes

- [Proxy / Extend and normalise the list of hosts excluded from the proxy](https://github.com/geonetwork/core-geonetwork/pull/9626): the default `excludeHosts` list now includes private network ranges, and the new `followRedirects` setting (disabled by default) controls whether the proxy follows redirects. Redirects from http to https for the same URL are still followed. Catalogues that use the proxy to reach hosts on a private network need to review these settings.

## List of changes

### New features

- [Metadata scheduled publication](https://github.com/geonetwork/core-geonetwork/pull/8948)
- [Make validation and publication options usable without the metadata approval workflow](https://github.com/geonetwork/core-geonetwork/pull/9420)
- [Set/unset metadata publication date when a metadata is published / unpublished](https://github.com/geonetwork/core-geonetwork/pull/9359)
- [Add a configurable welcome message to the home page](https://github.com/geonetwork/core-geonetwork/pull/9575)
- [Home page / Records mosaic improvements](https://github.com/geonetwork/core-geonetwork/pull/9574)
- [Record view / Add 'Add to favorites' button (saved selections)](https://github.com/geonetwork/core-geonetwork/pull/9582)
- [Add visibility options for record view custom menu items](https://github.com/geonetwork/core-geonetwork/pull/9415)
- [Show sub-portal logo, name and styles on the record permalink page](https://github.com/geonetwork/core-geonetwork/pull/9551)
- [Logo improvements](https://github.com/geonetwork/core-geonetwork/pull/9322)
- [Editor / Filestore / Add possibility to upload file from URL](https://github.com/geonetwork/core-geonetwork/pull/8794)
- [Allow rename files in metadata file store](https://github.com/geonetwork/core-geonetwork/pull/8965)
- [Metadata editor / Multiple selection for Link to Source metadata / Link to feature catalog dialogs](https://github.com/geonetwork/core-geonetwork/pull/9526)
- [Editor / Associated panel / Search exclude already associated records](https://github.com/geonetwork/core-geonetwork/pull/9065)
- [Offer the option to completely replace a record when overwriting on import](https://github.com/geonetwork/core-geonetwork/pull/9495)
- [Map viewer / Add support for WMS 1.1.x capabilities](https://github.com/geonetwork/core-geonetwork/pull/9595)
- [Map viewer / Make 'Add layers' options configurable](https://github.com/geonetwork/core-geonetwork/pull/9583)
- [Validation / Resolve XLinks based on configuration](https://github.com/geonetwork/core-geonetwork/pull/9585)
- [Allow reading the `createDate` index field from the XML document instead of Database](https://github.com/geonetwork/core-geonetwork/pull/9554)
- [Index external additional resource properties](https://github.com/geonetwork/core-geonetwork/pull/9112)
- [Record page / Series member ordered by last date](https://github.com/geonetwork/core-geonetwork/pull/9349)
- [Standard / ISO19115-3 / ISO19139 conversion](https://github.com/geonetwork/core-geonetwork/pull/9530)
- [Formatter / schema.org / Add croissant spec](https://github.com/geonetwork/core-geonetwork/pull/8939)
- [Formatter / DCAT / Improvements (Mobility and SEMIC-EU update)](https://github.com/geonetwork/core-geonetwork/pull/9111)
- [Harvester / ODS (Huwise) / Add support for custom asset and better support v1, 2.0 and 2.1 API](https://github.com/geonetwork/core-geonetwork/pull/9248)
- [Add support for using JSON Path in SimpleURL Harvester](https://github.com/geonetwork/core-geonetwork/pull/9132)
- [Index / Add Swedish language](https://github.com/geonetwork/core-geonetwork/pull/9355)

### Security

- [Unify error messages in user password recovery API](https://github.com/geonetwork/core-geonetwork/pull/9507)
- [Formatter / Use the shared cache only for anonymous requests](https://github.com/geonetwork/core-geonetwork/pull/9553)
- [Add method to retrieve records for use in untrusted XSLT](https://github.com/geonetwork/core-geonetwork/pull/9505)
- [Update property exclusions in User/UserSecurity XML format](https://github.com/geonetwork/core-geonetwork/pull/9504)
- [UI / Build typeahead suggestions as DOM text instead of HTML strings](https://github.com/geonetwork/core-geonetwork/pull/9508)
- [Fix static page link uuid/id injection](https://github.com/geonetwork/core-geonetwork/pull/9472)
- [Jolokia / Scope readable MBeans to GeoNetwork task metrics](https://github.com/geonetwork/core-geonetwork/pull/9509)
- Regions / CSW: configure an entity resolver on the GeoTools XML parsers (and upgrade to GeoTools 33.6)

### Fixes and improvements

- [Map viewer / Fix 3D mode with OpenLayers 10](https://github.com/geonetwork/core-geonetwork/pull/9601)
- [Map viewer / Fix export feature table with links](https://github.com/geonetwork/core-geonetwork/pull/9431)
- [Show layer title in download from WFS dropdown](https://github.com/geonetwork/core-geonetwork/pull/9572)
- [Logos / Fix blank source and site logos after a page reload](https://github.com/geonetwork/core-geonetwork/pull/9609)
- [UI / Fix site logo requests with an invalid URL](https://github.com/geonetwork/core-geonetwork/pull/9541)
- [Full view 'provided by' logo from group owner and images/harvesting](https://github.com/geonetwork/core-geonetwork/pull/9538)
- [Home page / Fix records mosaic gallery on small screens and keyboard focus](https://github.com/geonetwork/core-geonetwork/pull/9593)
- [Update the text and icon for saved selections to make them clearer](https://github.com/geonetwork/core-geonetwork/pull/9580)
- [Fix editor and search toolbar](https://github.com/geonetwork/core-geonetwork/pull/9556)
- [Improve working copy navigation and draft detection](https://github.com/geonetwork/core-geonetwork/pull/9532)
- [Editor / Show a contact-administrator message when no templates are available](https://github.com/geonetwork/core-geonetwork/pull/9559)
- [Metadata editor / Online resources / Fix mimeTypeStrategy parameter when adding a new online resource](https://github.com/geonetwork/core-geonetwork/pull/9461)
- [Schematron failed assert displayed using UI language in editor](https://github.com/geonetwork/core-geonetwork/pull/9534)
- [Pass metadataId to schematron](https://github.com/geonetwork/core-geonetwork/pull/9418)
- [Fix ui prevents invalid template publication](https://github.com/geonetwork/core-geonetwork/pull/9474)
- [Exclude templates from isValid and isApproved publication checks](https://github.com/geonetwork/core-geonetwork/pull/9422)
- [Fix for Metadata editors cannot submit record for review, but reviewers can](https://github.com/geonetwork/core-geonetwork/pull/8941)
- [Attachments API incorrectly marks draft resources as approved](https://github.com/geonetwork/core-geonetwork/pull/9443)
- [Preserve JCloud resource metadata when renaming](https://github.com/geonetwork/core-geonetwork/pull/9542)
- [Resource size label](https://github.com/geonetwork/core-geonetwork/pull/9494)
- [Fix DOI registration creating duplicate transferOptions sections](https://github.com/geonetwork/core-geonetwork/pull/9258)
- [Metadata selection export to PDF: Fix cover and intro pages](https://github.com/geonetwork/core-geonetwork/pull/9456)
- [Record page / Fix wrong display of parent metadata link for series metadata](https://github.com/geonetwork/core-geonetwork/pull/9468)
- [Metadata import / Make the owner group optional when checking if a group is a workspace](https://github.com/geonetwork/core-geonetwork/pull/9558)
- [Avoid unnecessary group type warnings in getOrCreateGroup](https://github.com/geonetwork/core-geonetwork/pull/9491)
- [Admin / Hide group translations section when creating a new group](https://github.com/geonetwork/core-geonetwork/pull/9568)
- [Auditing / Keep the creation user and date when a user is updated](https://github.com/geonetwork/core-geonetwork/pull/9594)
- [Fix ClassCastException in auditing when using Keycloak](https://github.com/geonetwork/core-geonetwork/pull/9570)
- [Update only the popularity column when a record is viewed](https://github.com/geonetwork/core-geonetwork/pull/9543)
- [Fix entries missing from index](https://github.com/geonetwork/core-geonetwork/pull/9425)
- [Indexing / Fix field type while changing sharing](https://github.com/geonetwork/core-geonetwork/pull/9513)
- [Index status / Report unreadable cluster health responses](https://github.com/geonetwork/core-geonetwork/pull/9478)
- [Statistics and status page / Stop endless polling of indexing task status](https://github.com/geonetwork/core-geonetwork/pull/9527)
- [Statistics and status page / Refresh the errors panel when indexing ends](https://github.com/geonetwork/core-geonetwork/pull/9567)
- [Health monitor / Fix free connections check with JNDI connection pools](https://github.com/geonetwork/core-geonetwork/pull/9571)
- [Failed warning health check when not using Kibana](https://github.com/geonetwork/core-geonetwork/pull/9492)
- [Metrics / Use the status of the response in the webapp metrics filter](https://github.com/geonetwork/core-geonetwork/pull/9552)
- [Monitoring / Fix WebappMetricsFilter error when stopping GeoNetwork with Ctrl+C (or SIGTERM)](https://github.com/geonetwork/core-geonetwork/pull/9528)
- [CSW / Remove unused Elasticsearch server dependency](https://github.com/geonetwork/core-geonetwork/pull/9577)
- [Harvester / Fix missing log file button in harvester history at TEST log level](https://github.com/geonetwork/core-geonetwork/pull/9529)
- [Harvested record still validated when UUID collision occurs](https://github.com/geonetwork/core-geonetwork/pull/9436)
- [Improve schematron validation error in harvesters, where file name is not provided](https://github.com/geonetwork/core-geonetwork/pull/9490)
- [Remove activemq in WFS Feature harvester](https://github.com/geonetwork/core-geonetwork/pull/9313)
- [Formatter / DCAT / Update date processing for dct:issued, dct:modified](https://github.com/geonetwork/core-geonetwork/pull/8869)
- [DCAT formatter / Fix dcat:distribution for associated service when the service metadata has no online resource](https://github.com/geonetwork/core-geonetwork/pull/9493)
- [Formatter / DCAT Mobility / Fix geodcatap namespace URL in XSL file](https://github.com/geonetwork/core-geonetwork/pull/9561)
- [Normalize organisation name value for ISO19110 metadata](https://github.com/geonetwork/core-geonetwork/pull/9469)
- Formatter / Datacite / Fix codelist value for relationType
- ISO19139 / Distribution format should not be multilingual
- Fix StackOverflowError adding gfc:FC_AssociationRole and similar feature catalogue elements
- Fix record author names in the editor board facet for users without surname
- Translations: [iso19139 German protocol labels](https://github.com/geonetwork/core-geonetwork/pull/9603), [schematron German translations](https://github.com/geonetwork/core-geonetwork/pull/9537), [feature catalogue Dutch translations](https://github.com/geonetwork/core-geonetwork/pull/9470), [groups API error messages](https://github.com/geonetwork/core-geonetwork/pull/9481), [Transifex updates](https://github.com/geonetwork/core-geonetwork/pull/9616)

### Documentation

- [Docs / Update system configuration and reorganize admin catalog pages](https://github.com/geonetwork/core-geonetwork/pull/9550)
- [Docs / CSW: document queryables mapping](https://github.com/geonetwork/core-geonetwork/pull/9576)
- [Docs / Document administrator password reset without old password](https://github.com/geonetwork/core-geonetwork/pull/9545)
- [Document the Languages and translations admin panel](https://github.com/geonetwork/core-geonetwork/pull/9458)
- [Configuration options discovered from source](https://github.com/geonetwork/core-geonetwork/pull/9110)
- [Document startup configuration of maxUploadSize](https://github.com/geonetwork/core-geonetwork/pull/8424)
- [Documentation / Search statistics / Mark setting as not functional](https://github.com/geonetwork/core-geonetwork/pull/9452)

### Library updates

- [Update OpenLayers to version 10.11.0](https://github.com/geonetwork/core-geonetwork/pull/9600)
- Upgrade GeoTools to 33.6, MapFish to 2.3.6, jwt-headers-util to GeoServer 2.27.7
- [commons-beanutils 1.11.0](https://github.com/geonetwork/core-geonetwork/pull/9483), [jsoup 1.23.1](https://github.com/geonetwork/core-geonetwork/pull/9475), [log4j-api 2.25.5](https://github.com/geonetwork/core-geonetwork/pull/9476)

and more \... see [4.4.13-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.13+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?q=is%3Apr+milestone%3A4.4.13+is%3Aclosed) for full details.
