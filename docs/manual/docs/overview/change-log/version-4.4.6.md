# Version 4.4.6 {#version-446}

GeoNetwork 4.4.6 is a minor release.

## Update notes

When updating please review the following actions:

### Index changes

Due to [Elasticsearch update to 8.14.3](https://github.com/geonetwork/core-geonetwork/pull/8337) it is recommended to use 8.14.x version of Elasticsearch server.

After updating use **Admin Console > Tools** and use **Delete index and reindex**:


## List of changes

Major changes:

* [Add support for external management named properties in JCloud](https://github.com/geonetwork/core-geonetwork/pull/8357)

* [Use UI language for metadata selection export to CSV / PDF.
  ](https://github.com/geonetwork/core-geonetwork/pull/8262)
 
* [WebDav harvester / Add support for XSLT filter process](https://github.com/geonetwork/core-geonetwork/pull/8243)

* [Register user / allow to configured allowed email domains](https://github.com/geonetwork/core-geonetwork/pull/8186)

* [Register user / allow to select the group where the user wants to register](https://github.com/geonetwork/core-geonetwork/pull/8176)

* [Support multiple DOI servers](https://github.com/geonetwork/core-geonetwork/pull/8098)

* [Standard / DCAT (and profiles) export ](https://github.com/geonetwork/core-geonetwork/pull/7600)

and more \... see [4.4.6-0 issues](https://github.com/geonetwork/core-geonetwork/issues?q=is%3Aissue+milestone%3A4.4.6+is%3Aclosed) and [pull requests](https://github.com/geonetwork/core-geonetwork/pulls?page=3&q=is%3Apr+milestone%3A4.4.6+is%3Aclosed) for full details.
